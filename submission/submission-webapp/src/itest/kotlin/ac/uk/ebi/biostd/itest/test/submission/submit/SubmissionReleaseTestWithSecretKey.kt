package ac.uk.ebi.biostd.itest.test.submission.submit

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.itest.ITestListener
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.submission.config.FilePersistenceConfig
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.coroutines.waitUntil
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
import ebi.ac.uk.io.ext.createFile
import ebi.ac.uk.util.date.atMidnight
import ebi.ac.uk.util.date.toStringDate
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfSystemProperty
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.io.File
import java.time.Duration
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Import(FilePersistenceConfig::class)
@ExtendWith(SpringExtension::class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["app.persistence.includeSecretKey=true", "app.persistence.nfsReleaseMode=MOVE"],
)
@DirtiesContext
class SubmissionReleaseTestWithSecretKey(
    @LocalServerPort val serverPort: Int,
    @Autowired val securityTestService: SecurityTestService,
    @Autowired val submissionRepository: SubmissionPersistenceQueryService,
    @Autowired val toSubmissionMapper: ToSubmissionMapper,
) {
    private lateinit var webClient: BioWebClient

    @BeforeAll
    fun init(): Unit =
        runBlocking {
            securityTestService.ensureUserRegistration(SuperUser)
            webClient = getWebClient(serverPort, SuperUser)
        }

    @Test
    @EnabledIfSystemProperty(named = "enableFire", matches = "false")
    fun `27-3 public submission with secret key and MOVE release mode`() =
        runTest {
            val submission =
                tsv {
                    line("Submission", "S-RELEASE003")
                    line("Title", "Submission")
                    line("ReleaseDate", OffsetDateTime.now().toStringDate())
                    line()

                    line("Study")
                    line()

                    line("File", "file_27-3.txt")
                    line()
                }.toString()

            webClient.uploadFile(ITestListener.tempFolder.createFile("file_27-3.txt", "27-3 file content"))
            assertThat(webClient.submitSingle(submission, SubmissionFormat.TSV)).isSuccessful()

            val submitted = submissionRepository.getExtByAccNo("S-RELEASE003")

            val expectedFile = File("${ITestListener.ftpPath}/${submitted.relPath}/Files/file_27-3.txt")
            Assertions.assertThat(expectedFile).exists()
            Assertions.assertThat(expectedFile).hasContent("27-3 file content")

            val key = submitted.secretKey
            val subFilesPath =
                "${ITestListener.submissionPath}/${key.take(2)}/${key.substring(2)}/${submitted.relPath}/Files"
            Assertions.assertThat(File("$subFilesPath/file_27-3.txt")).doesNotExist()
        }

    @Test
    @EnabledIfSystemProperty(named = "enableFire", matches = "false")
    fun `27-4 private submission with secret key and MOVE release mode`() =
        runTest {
            val submission =
                tsv {
                    line("Submission", "S-RELEASE004")
                    line("Title", "Submission")
                    line("ReleaseDate", "2030-01-25")
                    line()

                    line("Study")
                    line()

                    line("File", "file_27-4.txt")
                    line()
                }.toString()

            webClient.uploadFile(ITestListener.tempFolder.createFile("file_27-4.txt", "27-4 file content"))
            assertThat(webClient.submitSingle(submission, SubmissionFormat.TSV)).isSuccessful()

            val submitted = submissionRepository.getExtByAccNo("S-RELEASE004")

            Assertions.assertThat(File("${ITestListener.ftpPath}/${submitted.relPath}")).doesNotExist()

            val key = submitted.secretKey
            val subFilesPath =
                "${ITestListener.submissionPath}/${key.take(2)}/${key.substring(2)}/${submitted.relPath}/Files"
            val expectedFile = File("$subFilesPath/file_27-4.txt")
            Assertions.assertThat(expectedFile).exists()
            Assertions.assertThat(expectedFile).hasContent("27-4 file content")
        }

    @Test
    fun `27-5 release already submitted submission using release operation`() =
        runTest {
            val accNo = "SR-001"
            val releaseTime = OffsetDateTime.of(2050, 9, 21, 15, 0, 0, 0, ZoneOffset.UTC)
            val newRelease = OffsetDateTime.of(2010, 9, 21, 15, 0, 0, 0, ZoneOffset.UTC)

            val submission =
                tsv {
                    line("Submission", accNo)
                    line("Title", "Submission")
                    line("ReleaseDate", releaseTime.toStringDate())
                }.toString()

            webClient.submitSingle(submission, SubmissionFormat.TSV)

            val submitted = submissionRepository.getExtByAccNo(accNo)
            Assertions.assertThat(submitted.releaseTime).isEqualTo(releaseTime.atMidnight())
            Assertions.assertThat(submitted.released).isEqualTo(false)

            val (rqtAccNo, rqtVersion) = webClient.releaseSubmission(accNo, newRelease.toInstant())

            waitUntil(timeout = Duration.ofSeconds(10)) {
                submissionRepository.existByAccNoAndVersion(
                    rqtAccNo,
                    rqtVersion,
                )
            }
            val newVersion = submissionRepository.getExtByAccNo(accNo)
            Assertions.assertThat(newVersion.releaseTime).isEqualTo(newRelease.atMidnight())
            Assertions.assertThat(newVersion.released).isEqualTo(true)
        }
}
