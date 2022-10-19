package ac.uk.ebi.biostd.itest.test.submission.submit

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SubmissionFilesConfig
import ac.uk.ebi.biostd.common.config.FilePersistenceConfig
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.enableFire
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.storageMode
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.submissionPath
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.tempFolder
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.filesystem.fire.ZipUtil
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.dsl.file
import ebi.ac.uk.dsl.section
import ebi.ac.uk.dsl.submission
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
import ebi.ac.uk.extended.model.ExtFileType
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.allSectionsFiles
import ebi.ac.uk.io.ext.allSubFiles
import ebi.ac.uk.io.ext.createDirectory
import ebi.ac.uk.io.ext.createFile
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.sources.PreferredSource.FIRE
import ebi.ac.uk.io.sources.PreferredSource.SUBMISSION
import ebi.ac.uk.io.sources.PreferredSource.USER_SPACE
import ebi.ac.uk.model.extensions.title
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfSystemProperty
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Transactional
import uk.ac.ebi.fire.client.integration.web.FireClient
import java.io.File
import java.nio.file.Paths

@Import(FilePersistenceConfig::class)
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
class SubmissionFileSourceTest(
    @Autowired private val submissionRepository: SubmissionPersistenceQueryService,
    @Autowired private val securityTestService: SecurityTestService,
    @Autowired val toSubmissionMapper: ToSubmissionMapper,
    @Autowired private val fireClient: FireClient,
    @LocalServerPort val serverPort: Int,
) {
    private lateinit var webClient: BioWebClient

    @BeforeAll
    fun init() {
        securityTestService.ensureUserRegistration(SuperUser)
        webClient = getWebClient(serverPort, SuperUser)
    }

    @Test
    fun `resubmission with SUBMISSION file source as priority over USER_SPACE`() {
        fun submission(fileList: String) = tsv {
            line("Submission", "S-FSTST1")
            line("Title", "Preferred Source Submission")
            line()

            line("Study", "SECT-001")
            line("Title", "Root Section")
            line("File List", fileList)
            line()

            line("File", "File1.txt")

            line()
        }.toString()

        val fileList = tempFolder.createFile(
            "FileList.tsv",
            tsv {
                line("Files", "GEN")
                line("File2.txt", "ABC")
            }.toString()
        )

        val file4 = tempFolder.createFile("File1.txt", "content file 1")
        val file5 = tempFolder.createFile("File2.txt", "content file 2")
        val filesConfig = SubmissionFilesConfig(listOf(fileList, file4, file5), storageMode)
        assertThat(webClient.submitSingle(submission("FileList.tsv"), TSV, filesConfig)).isSuccessful()

        val firstVersion = submissionRepository.getExtByAccNo("S-FSTST1")
        val firstVersionReferencedFiles = submissionRepository.getReferencedFiles("S-FSTST1", "FileList")
        val subFilesPath = "$submissionPath/${firstVersion.relPath}/Files"
        val innerFile = Paths.get("$subFilesPath/File1.txt")
        val referencedFile = Paths.get("$subFilesPath/File2.txt")

        assertThat(innerFile).exists()
        assertThat(innerFile.toFile().readText()).isEqualTo("content file 1")
        assertThat(referencedFile).exists()
        assertThat(referencedFile.toFile().readText()).isEqualTo("content file 2")

        file4.delete()
        file5.delete()
        fileList.delete()

        tempFolder.createFile("File1.txt", "content file 1 updated")

        val reSubFilesConfig =
            SubmissionFilesConfig(emptyList(), storageMode, preferredSources = listOf(SUBMISSION, USER_SPACE))
        assertThat(
            webClient.submitSingle(
                submission("FileList.json"),
                TSV,
                reSubFilesConfig
            )
        ).isSuccessful()
        assertThat(innerFile.toFile().readText()).isEqualTo("content file 1")
        assertThat(referencedFile.toFile().readText()).isEqualTo("content file 2")

        if (enableFire) {
            val secondVersion = submissionRepository.getExtByAccNo("S-FSTST1")
            val secondVersionReferencedFiles = submissionRepository.getReferencedFiles("S-FSTST1", "FileList")

            val firstVersionFireId = (firstVersion.allSectionsFiles.first() as FireFile).fireId
            val secondVersionFireId = (secondVersion.allSectionsFiles.first() as FireFile).fireId
            assertThat(firstVersionFireId).isEqualTo(secondVersionFireId)

            val firstVersionReferencedFireId = (firstVersionReferencedFiles.first() as FireFile).fireId
            val secondVersionReferencedFireId = (secondVersionReferencedFiles.first() as FireFile).fireId
            assertThat(firstVersionReferencedFireId).isEqualTo(secondVersionReferencedFireId)
        }
    }

    @Test
    @EnabledIfSystemProperty(named = "enableFire", matches = "true")
    fun `submission with FIRE source only`() {
        val file3 = tempFolder.createFile("File3.txt", "content file 3")
        val file4 = tempFolder.createFile("File4.txt", "content file 4")
        val file3Md5 = file3.md5()
        val file4Md5 = file4.md5()

        val fireFile3 = fireClient.save(file3, file3Md5, 55L)
        val fireFile4 = fireClient.save(file4, file4Md5, 55L)

        val submission = tsv {
            line("Submission", "S-FSTST2")
            line("Title", "FIRE Only File Source Submission")
            line()

            line("Study", "SECT-001")
            line("Title", "Root Section")
            line("File List", "FileList.tsv")
            line()

            line("File", "File4.txt")
            line("md5", file4Md5)
            line()

            line()
        }.toString()

        val fileList = tempFolder.createFile(
            "FileList.tsv",
            tsv {
                line("Files", "GEN", "md5")
                line("File3.txt", "ABC", file3Md5)
            }.toString()
        )

        val filesConfig =
            SubmissionFilesConfig(listOf(fileList), storageMode, preferredSources = listOf(FIRE))
        assertThat(webClient.submitSingle(submission, TSV, filesConfig)).isSuccessful()

        val persistedSubmission = submissionRepository.getExtByAccNo("S-FSTST2")
        val firstVersionReferencedFiles = submissionRepository.getReferencedFiles("S-FSTST2", "FileList")
        val subFilesPath = "$submissionPath/${persistedSubmission.relPath}/Files"
        val innerFile = Paths.get("$subFilesPath/File4.txt")
        val referencedFile = Paths.get("$subFilesPath/File3.txt")

        assertThat(innerFile).exists()
        assertThat(innerFile.toFile().readText()).isEqualTo("content file 4")
        assertThat(referencedFile).exists()
        assertThat(referencedFile.toFile().readText()).isEqualTo("content file 3")

        val innerFileFireId = (persistedSubmission.allSectionsFiles.first() as FireFile).fireId
        assertThat(innerFileFireId).isEqualTo(fireFile4.fireOid)

        val referencedFileFireId = (firstVersionReferencedFiles.first() as FireFile).fireId
        assertThat(referencedFileFireId).isEqualTo(fireFile3.fireOid)
    }

    @Test
    @EnabledIfSystemProperty(named = "enableFire", matches = "true")
    fun `submission with directory with files on FIRE`() {
        val submission = tsv {
            line("Submission", "S-FSTST3")
            line("Title", "Simple Submission With directory")
            line()

            line("Study")
            line()

            line("File", "directory")
            line("Type", "test")
            line()
        }.toString()

        val file1 = tempFolder.createFile("file1.txt", "content-1")
        val file2 = tempFolder.createFile("file2.txt", "content-2")

        webClient.uploadFiles(listOf(file1), "directory")
        webClient.uploadFiles(listOf(file2), "directory/subdirectory")

        assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()

        val submitted = submissionRepository.getExtByAccNo("S-FSTST3")
        assertThat(submitted.section.files).hasSize(1)
        assertThat(submitted.section.files.first()).hasLeftValueSatisfying {
            assertThat(it.type).isEqualTo(ExtFileType.DIR)
            assertThat(it.size).isEqualTo(326L)
            assertThat(it.md5).isEqualTo("8BD1F30C5389037D06A3CA20E5549B45")

            val subZip = tempFolder.createDirectory("target")
            ZipUtil.unpack(File("$submissionPath/${submitted.relPath}/Files/directory.zip"), subZip)
            val files = subZip.allSubFiles()
                .filter { file -> file.isDirectory.not() }
                .map { file -> file.toRelativeString(subZip) to file.readText() }

            assertThat(files).containsExactly(
                "file1.txt" to file1.readText(),
                "subdirectory/file2.txt" to file2.readText()
            )
        }
    }

    @Test
    fun `multiple file references`() {
        val submission = tsv {
            line("Submission", "S-FSTST4")
            line("Title", "Simple Submission With Files")
            line("ReleaseDate", "2020-01-25")
            line()

            line("Study")
            line("Type", "Experiment")
            line()

            line("File", "multiple-references.txt")
            line("Type", "test")
            line()

            line("Experiment", "Exp1")
            line("Type", "Subsection")
            line()

            line("File", "multiple-references.txt")
            line("Type", "Second reference")
            line()
        }.toString()

        webClient.uploadFiles(listOf(tempFolder.createFile("multiple-references.txt")))

        assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()

        val submitted = submissionRepository.getExtByAccNo("S-FSTST4")
        assertThat(submitted.version).isEqualTo(1)
        assertThat(File("$submissionPath/${submitted.relPath}/Files/multiple-references.txt")).exists()
    }

    @Test
    fun `submission with group file`() {
        val groupName = "The-Group"
        val submission = tsv {
            line("Submission", "S-FSTST5")
            line("Title", "Sample Submission")
            line()

            line("Study")
            line()

            line("File", "groups/$groupName/GroupFile1.txt")
            line()

            line("File", "groups/$groupName/folder/GroupFile2.txt")
            line()
        }.toString()

        webClient.addUserInGroup(webClient.createGroup(groupName, "group-desc").name, SuperUser.email)
        webClient.uploadGroupFiles(groupName, listOf(tempFolder.createFile("GroupFile1.txt")))
        webClient.uploadGroupFiles(groupName, listOf(tempFolder.createFile("GroupFile2.txt")), "folder")

        assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()
        val submitted = toSubmissionMapper.toSimpleSubmission(submissionRepository.getExtByAccNo("S-FSTST5"))
        assertThat(submitted).isEqualTo(
            submission("S-FSTST5") {
                title = "Sample Submission"
                section("Study") {
                    file("groups/$groupName/GroupFile1.txt")
                    file("groups/$groupName/folder/GroupFile2.txt")
                }
            }
        )
    }
}
