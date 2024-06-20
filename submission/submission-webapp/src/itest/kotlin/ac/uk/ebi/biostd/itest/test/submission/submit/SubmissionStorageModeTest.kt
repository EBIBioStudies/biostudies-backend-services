package ac.uk.ebi.biostd.itest.test.submission.submit

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.fireFtpPath
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.fireSubmissionPath
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.nfsFtpPath
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.nfsSubmissionPath
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.tempFolder
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.doc.db.reactive.repositories.SubmissionRequestRepository
import ac.uk.ebi.biostd.persistence.model.DbSequence
import ac.uk.ebi.biostd.persistence.repositories.SequenceDataRepository
import ac.uk.ebi.biostd.submission.config.FilePersistenceConfig
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.coroutines.waitUntil
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.extended.model.StorageMode.FIRE
import ebi.ac.uk.extended.model.StorageMode.NFS
import ebi.ac.uk.io.ext.createFile
import ebi.ac.uk.io.ext.listFilesOrEmpty
import ebi.ac.uk.model.RequestStatus.PROCESSED
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import java.io.File
import java.time.Duration.ofSeconds
import kotlin.reflect.KClass

@Import(FilePersistenceConfig::class)
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SubmissionStorageModeTest(
    @Autowired val securityTestService: SecurityTestService,
    @Autowired val submissionRequestRepository: SubmissionRequestRepository,
    @Autowired val submissionRepository: SubmissionPersistenceQueryService,
    @Autowired val serializationService: ExtSerializationService,
    @Autowired val sequenceRepository: SequenceDataRepository,
    @LocalServerPort val serverPort: Int,
) {
    private lateinit var webClient: BioWebClient

    @BeforeAll
    fun init(): Unit =
        runBlocking {
            securityTestService.ensureUserRegistration(SuperUser)
            webClient = getWebClient(serverPort, SuperUser)
            sequenceRepository.save(DbSequence("S-STR-MODE"))
        }

    @Test
    fun `10-1 Fire to Nfs`() =
        runTest {
            val (submission, file, fileList, fileListFile) = createSubmission("S-STR-MODE-1")
            webClient.uploadFiles(listOf(file, fileListFile, fileList))

            assertThat(webClient.submitSingle(submission, TSV, FIRE)).isSuccessful()
            val fireSub = submissionRepository.getExtByAccNo("S-STR-MODE-1", includeFileListFiles = true)
            assertThat(fireSub.storageMode).isEqualTo(FIRE)
            assertThat(fireSub.version).isEqualTo(1)

            assertThat(fireSub.section.files.first()).hasLeftValueSatisfying { assertFile(it, FireFile::class) }
            assertThat(fireSub.section.fileList).isNotNull()
            assertFileListFile(fireSub.section.fileList!!, FireFile::class)

            assertThat(webClient.submitSingle(submission, TSV, NFS)).isSuccessful()
            val nfsSub = submissionRepository.getExtByAccNo("S-STR-MODE-1", includeFileListFiles = true)
            assertThat(nfsSub.storageMode).isEqualTo(NFS)
            assertThat(nfsSub.version).isEqualTo(2)

            assertThat(nfsSub.section.files.first()).hasLeftValueSatisfying { assertFile(it, NfsFile::class) }
            assertThat(nfsSub.section.fileList).isNotNull()
            assertFileListFile(nfsSub.section.fileList!!, NfsFile::class)

            // No Files in FIRE submit folder/ftp folder
            assertThat(fireSubmissionPath.resolve(fireSub.relPath).listFilesOrEmpty().filter { it.isFile }).isEmpty()
            assertThat(fireFtpPath.resolve(fireSub.relPath).listFilesOrEmpty().filter { it.isFile }).isEmpty()
        }

    @Test
    fun `10-2 Nfs to Fire`() =
        runTest {
            val (submission, file, fileList, fileListFile) = createSubmission("S-STR-MODE-2")
            webClient.uploadFiles(listOf(file, fileListFile, fileList))

            assertThat(webClient.submitSingle(submission, TSV, NFS)).isSuccessful()
            val nfsSub = submissionRepository.getExtByAccNo("S-STR-MODE-2", includeFileListFiles = true)
            assertThat(nfsSub.storageMode).isEqualTo(NFS)
            assertThat(nfsSub.version).isEqualTo(1)

            assertThat(nfsSub.section.files.first()).hasLeftValueSatisfying { assertFile(it, NfsFile::class) }
            assertThat(nfsSub.section.fileList).isNotNull()
            assertFileListFile(nfsSub.section.fileList!!, NfsFile::class)

            assertThat(webClient.submitSingle(submission, TSV, FIRE)).isSuccessful()
            val fireSub = submissionRepository.getExtByAccNo("S-STR-MODE-2", includeFileListFiles = true)
            assertThat(fireSub.version).isEqualTo(2)
            assertThat(fireSub.storageMode).isEqualTo(FIRE)

            assertThat(fireSub.section.files.first()).hasLeftValueSatisfying { assertFile(it, FireFile::class) }
            assertThat(fireSub.section.fileList).isNotNull()
            assertFileListFile(fireSub.section.fileList!!, FireFile::class)

            // No Files in NFS submit folder/ftp
            assertThat(nfsSubmissionPath.resolve(nfsSub.relPath).listFilesOrEmpty().filter { it.isFile }).isEmpty()
            assertThat(nfsFtpPath.resolve(nfsSub.relPath).listFilesOrEmpty().filter { it.isFile }).isEmpty()
        }

    @Test
    fun `10-3 transfer from NFS to FIRE`() =
        runTest {
            val (submission, file, fileList, fileListFile) = createSubmission("S-STR-MODE-3")
            webClient.uploadFiles(listOf(file, fileListFile, fileList))

            assertThat(webClient.submitSingle(submission, TSV, NFS)).isSuccessful()
            val nfsSub = submissionRepository.getExtByAccNo("S-STR-MODE-3", includeFileListFiles = true)

            webClient.transferSubmission("S-STR-MODE-3", FIRE)
            waitUntil(ofSeconds(10)) {
                submissionRequestRepository.getByAccNoAndVersion("S-STR-MODE-3", 2).status == PROCESSED
            }

            val fireSub = submissionRepository.getExtByAccNo("S-STR-MODE-3", includeFileListFiles = true)
            assertThat(fireSub.storageMode).isEqualTo(FIRE)
            assertThat(fireSub.version).isEqualTo(2)

            assertThat(fireSub.section.files.first()).hasLeftValueSatisfying { assertFile(it, FireFile::class) }
            assertThat(fireSub.section.fileList).isNotNull()
            assertFileListFile(fireSub.section.fileList!!, FireFile::class)

            assertThat(nfsSubmissionPath.resolve(nfsSub.relPath).listFilesOrEmpty().filter { it.isFile }).isEmpty()
            assertThat(nfsFtpPath.resolve(nfsSub.relPath).listFilesOrEmpty().filter { it.isFile }).isEmpty()
        }

    @Test
    fun `10-4 transfer from FIRE to NFS`() =
        runTest {
            val (submission, file, fileList, fileListFile) = createSubmission("S-STR-MODE-4")
            webClient.uploadFiles(listOf(file, fileListFile, fileList))
            assertThat(webClient.submitSingle(submission, TSV, FIRE)).isSuccessful()
            val fireSub = submissionRepository.getExtByAccNo("S-STR-MODE-4", includeFileListFiles = true)

            webClient.transferSubmission("S-STR-MODE-4", NFS)

            waitUntil(ofSeconds(10)) {
                submissionRequestRepository.getByAccNoAndVersion("S-STR-MODE-4", 2).status == PROCESSED
            }

            val nfsSub = submissionRepository.getExtByAccNo("S-STR-MODE-4", includeFileListFiles = true)

            assertThat(nfsSub.storageMode).isEqualTo(NFS)
            assertThat(nfsSub.version).isEqualTo(2)

            assertThat(nfsSub.section.files.first()).hasLeftValueSatisfying { assertFile(it, NfsFile::class) }
            assertThat(nfsSub.section.fileList).isNotNull()
            assertFileListFile(nfsSub.section.fileList!!, NfsFile::class)

            assertThat(fireSubmissionPath.resolve(fireSub.relPath).listFilesOrEmpty().filter { it.isFile }).isEmpty()
            assertThat(fireFtpPath.resolve(fireSub.relPath).listFilesOrEmpty().filter { it.isFile }).isEmpty()
        }

    private fun assertFile(
        file: ExtFile,
        expectType: KClass<*>,
    ) {
        assertThat(file.fileName).isEqualTo("one_file.txt")
        assertThat(file.attributes.first()).isEqualTo(ExtAttribute("Type", "test"))
        assertThat(file).isInstanceOf(expectType.java)
    }

    private suspend fun assertFileListFile(
        fileList: ExtFileList,
        expectType: KClass<*>,
    ) {
        assertThat(fileList.fileName).isEqualTo("file-list")

        val files = fileList.file.inputStream().use { serializationService.deserializeListAsFlow(it).toList() }
        assertThat(files).hasSize(1)

        val file = files.first()
        assertThat(file.fileName).isEqualTo("file-list-file.txt")
        assertThat(file.attributes.first()).isEqualTo(ExtAttribute("Type2", "file-list-test"))
        assertThat(file).isInstanceOf(expectType.java)
    }

    private fun createSubmission(accNo: String): Submission {
        val fileList =
            tempFolder.createFile(
                "file-list.tsv",
                tsv {
                    line("Files", "Type2")
                    line("file-list-file.txt", "file-list-test")
                }.toString(),
            )

        val submission =
            tsv {
                line("Submission", accNo)
                line("Title", "Storage mode submission")
                line()

                line("Study")
                line("Type", "Experiment")
                line("File List", fileList.name)
                line()
                line("File", "one_file.txt")
                line("Type", "test")
                line()
            }.toString()

        return Submission(
            submission = submission,
            file = tempFolder.createFile("one_file.txt", "content"),
            fileList = fileList,
            fileListFile = tempFolder.createFile("file-list-file.txt", "another content"),
        )
    }

    private data class Submission(
        val submission: String,
        val file: File,
        val fileList: File,
        val fileListFile: File,
    )
}
