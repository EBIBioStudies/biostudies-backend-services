package ac.uk.ebi.biostd.itest.test.submission.submit

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.ftpPath
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.submissionPath
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.tempFolder
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.submission.config.FilePersistenceConfig
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
import ebi.ac.uk.io.FileUtils
import ebi.ac.uk.io.ext.createFile
import ebi.ac.uk.util.date.toStringDate
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfSystemProperty
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.io.File
import java.time.OffsetDateTime

@Import(FilePersistenceConfig::class)
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
class SubmissionReleaseTest(
    @Autowired val securityTestService: SecurityTestService,
    @Autowired val submissionRepository: SubmissionPersistenceQueryService,
    @Autowired val toSubmissionMapper: ToSubmissionMapper,
    @LocalServerPort val serverPort: Int,
) {
    private lateinit var webClient: BioWebClient

    @BeforeAll
    fun init(): Unit = runBlocking {
        securityTestService.ensureUserRegistration(SuperUser)
        webClient = getWebClient(serverPort, SuperUser)
    }

    @Nested
    @SpringBootTest(
        webEnvironment = RANDOM_PORT,
        properties = ["app.persistence.includeSecretKey=false", "app.persistence.nfsReleaseMode=HARD_LINKS"],
    )
    inner class WithoutSecretKey(
        @LocalServerPort val serverPort: Int,
    ) {
        @Test
        @EnabledIfSystemProperty(named = "enableFire", matches = "false")
        fun `27-1 public submission without secret key and HARD_LINKS release mode`() = runTest {
            val submission = tsv {
                line("Submission", "S-RELEASE001")
                line("Title", "Submission")
                line("ReleaseDate", OffsetDateTime.now().toStringDate())
                line()

                line("Study")
                line()

                line("File", "file_27-1.txt")
                line()
            }.toString()

            webClient.uploadFile(tempFolder.createFile("file_27-1.txt", "27-1 file content"))
            assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()

            val submitted = submissionRepository.getExtByAccNo("S-RELEASE001")

            val ftpFiles = FileUtils.listAllFiles(File("$ftpPath/${submitted.relPath}/Files"))
            val expectedFile = File("$ftpPath/${submitted.relPath}/Files/file_27-1.txt")
            assertThat(ftpFiles).containsExactly(expectedFile)
            assertThat(expectedFile).hasContent("27-1 file content")
            assertThat(File("$submissionPath/${submitted.relPath}/Files/file_27-1.txt")).hasContent("27-1 file content")
        }

        @Test
        @EnabledIfSystemProperty(named = "enableFire", matches = "false")
        fun `27-2 private submission without secret key and HARD_LINKS release mode`() = runTest {
            val submission = tsv {
                line("Submission", "S-RELEASE002")
                line("Title", "Submission")
                line("ReleaseDate", "2030-01-25")
                line()

                line("Study")
                line()

                line("File", "file_27-2.txt")
                line()
            }.toString()

            webClient.uploadFile(tempFolder.createFile("file_27-2.txt", "27-2 file content"))
            assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()

            val submitted = submissionRepository.getExtByAccNo("S-RELEASE002")

            assertThat(File("$ftpPath/${submitted.relPath}")).doesNotExist()
            val submissionFiles = FileUtils.listAllFiles(File("$submissionPath/${submitted.relPath}/Files"))
            val expectedFile = File("$submissionPath/${submitted.relPath}/Files/file_27-2.txt")
            assertThat(submissionFiles).containsOnly(expectedFile)
            assertThat(expectedFile).hasContent("27-2 file content")
        }
    }

    @Nested
    @SpringBootTest(
        webEnvironment = RANDOM_PORT,
        properties = ["app.persistence.includeSecretKey=true", "app.persistence.nfsReleaseMode=MOVE"],
    )
    inner class WithSecretKey(
        @LocalServerPort val serverPort: Int,
    ) {
        @Test
        @EnabledIfSystemProperty(named = "enableFire", matches = "false")
        fun `27-3 public submission with secret key and MOVE release mode`() = runTest {
            val submission = tsv {
                line("Submission", "S-RELEASE003")
                line("Title", "Submission")
                line("ReleaseDate", OffsetDateTime.now().toStringDate())
                line()

                line("Study")
                line()

                line("File", "file_27-3.txt")
                line()
            }.toString()

            webClient.uploadFile(tempFolder.createFile("file_27-3.txt", "27-3 file content"))
            assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()

            val submitted = submissionRepository.getExtByAccNo("S-RELEASE003")

            val expectedFile = File("$ftpPath/${submitted.relPath}/Files/file_27-3.txt")
            assertThat(expectedFile).exists()
            assertThat(expectedFile).hasContent("27-3 file content")

            val key = submitted.secretKey
            val subFilesPath = "$submissionPath/${key.take(2)}/${key.substring(2)}/${submitted.relPath}/Files"
            assertThat(File("$subFilesPath/file_27-3.txt")).doesNotExist()
        }

        @Test
        @EnabledIfSystemProperty(named = "enableFire", matches = "false")
        fun `27-4 private submission with secret key and MOVE release mode`() = runTest {
            val submission = tsv {
                line("Submission", "S-RELEASE004")
                line("Title", "Submission")
                line("ReleaseDate", "2030-01-25")
                line()

                line("Study")
                line()

                line("File", "file_27-4.txt")
                line()
            }.toString()

            webClient.uploadFile(tempFolder.createFile("file_27-4.txt", "27-4 file content"))
            assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()

            val submitted = submissionRepository.getExtByAccNo("S-RELEASE004")

            assertThat(File("$ftpPath/${submitted.relPath}")).doesNotExist()

            val key = submitted.secretKey
            val subFilesPath = "$submissionPath/${key.take(2)}/${key.substring(2)}/${submitted.relPath}/Files"
            val expectedFile = File("$subFilesPath/file_27-4.txt")
            assertThat(expectedFile).exists()
            assertThat(expectedFile).hasContent("27-4 file content")
        }
    }
}
