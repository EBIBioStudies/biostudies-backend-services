package ac.uk.ebi.biostd.itest.test.submission.submit

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.common.config.PersistenceConfig
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.fireFtpPath
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.fireSubmissionPath
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.nfsFtpPath
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.nfsSubmissionPath
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.tempFolder
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.model.DbSequence
import ac.uk.ebi.biostd.persistence.repositories.SequenceDataRepository
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.extended.model.StorageMode.FIRE
import ebi.ac.uk.extended.model.StorageMode.NFS
import ebi.ac.uk.io.ext.asFileList
import ebi.ac.uk.io.ext.createFile
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
import kotlin.reflect.KClass

@Import(PersistenceConfig::class)
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SubmissionStorageModeTest(
    @Autowired val securityTestService: SecurityTestService,
    @Autowired val submissionRepository: SubmissionPersistenceQueryService,
    @Autowired val serializationService: ExtSerializationService,
    @Autowired val sequenceRepository: SequenceDataRepository,
    @LocalServerPort val serverPort: Int,
) {
    private lateinit var webClient: BioWebClient

    @BeforeAll
    fun init() {
        securityTestService.ensureUserRegistration(SuperUser)
        webClient = getWebClient(serverPort, SuperUser)
        sequenceRepository.save(DbSequence("S-STR-MODE"))
    }

    @Test
    fun `Fire to Nfs`() {
        val submission = createSubmission("S-STR-MODE-1")

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
        assertThat(fireSubmissionPath.resolve(fireSub.relPath).asFileList().filter { it.isFile }).isEmpty()
        assertThat(fireFtpPath.resolve(fireSub.relPath).asFileList().filter { it.isFile }).isEmpty()
    }

    @Test
    fun `Nfs to Fire`() {
        val submission = createSubmission("S-STR-MODE-2")

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
        assertThat(nfsSubmissionPath.resolve(fireSub.relPath).asFileList().filter { it.isFile }).isEmpty()
        assertThat(nfsFtpPath.resolve(fireSub.relPath).asFileList().filter { it.isFile }).isEmpty()
    }

    private fun assertFile(file: ExtFile, expectType: KClass<*>) {
        assertThat(file.fileName).isEqualTo("one_file.txt")
        assertThat(file.attributes.first()).isEqualTo(ExtAttribute("Type", "test"))
        assertThat(file).isInstanceOf(expectType.java)
    }

    private fun assertFileListFile(fileList: ExtFileList, expectType: KClass<*>) {
        assertThat(fileList.fileName).isEqualTo("file-list")

        val files = fileList.file.inputStream().use { serializationService.deserializeList(it).toList() }
        assertThat(files).hasSize(1)

        val file = files.first()
        assertThat(file.fileName).isEqualTo("file-list-file.txt")
        assertThat(file.attributes.first()).isEqualTo(ExtAttribute("Type2", "file-list-test"))
        assertThat(file).isInstanceOf(expectType.java)
    }

    private fun createSubmission(accNo: String): String {
        val fileList = tempFolder.createFile(
            "file-list.tsv",
            tsv {
                line("Files", "Type2")
                line("file-list-file.txt", "file-list-test")
            }.toString()
        )

        val submission = tsv {
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

        webClient.uploadFiles(
            listOf(
                tempFolder.createFile("one_file.txt", "content"),
                tempFolder.createFile("file-list-file.txt", "another content"),
                fileList
            )
        )

        return submission
    }
}