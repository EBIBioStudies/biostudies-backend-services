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
import ebi.ac.uk.api.SubmitParameters
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.coroutines.waitUntil
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
import ebi.ac.uk.io.ext.createFile
import ebi.ac.uk.io.sources.PreferredSource
import ebi.ac.uk.util.date.atMidnight
import ebi.ac.uk.util.date.toStringDate
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
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
class SubmissionReleaseSecretKeyTest(
    @param:LocalServerPort val serverPort: Int,
    @param:Autowired val securityTestService: SecurityTestService,
    @param:Autowired val submissionRepository: SubmissionPersistenceQueryService,
    @param:Autowired val toSubmissionMapper: ToSubmissionMapper,
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

                    line("File", "27-3_dir")
                    line()
                }.toString()

            webClient.uploadFile(tempFolder.createFile("file_27-3.txt", "27-3 file content"))
            webClient.uploadFile(tempFolder.createFile("file_27-3_2.txt", "27-3-2 file content"), "27-3_dir")
            assertThat(webClient.submit(submission, TSV)).isSuccessful()

            val submitted = submissionRepository.getExtByAccNo("S-RELEASE003")

            val expectedFile = File("$ftpPath/${submitted.relPath}/Files/file_27-3.txt")
            assertThat(expectedFile).exists()
            assertThat(expectedFile).hasContent("27-3 file content")

            val expectedInnerFile = File("$ftpPath/${submitted.relPath}/Files/27-3_dir/file_27-3_2.txt")
            assertThat(expectedInnerFile).exists()
            assertThat(expectedInnerFile).hasContent("27-3-2 file content")

            val key = submitted.secretKey
            val subFilesPath =
                "$submissionPath/${key.take(2)}/${key.substring(2)}/${submitted.relPath}/Files"
            assertThat(File("$subFilesPath/file_27-3.txt")).doesNotExist()
            assertThat(File("$subFilesPath/27-3_dir/file_27-3_2.txt")).doesNotExist()
        }

    @Test
    @EnabledIfSystemProperty(named = "enableFire", matches = "false")
    fun `27-4 release private submission with secret key and MOVE release mode`() =
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

                    line("File", "27-4_dir")
                    line()
                }.toString()

            webClient.uploadFile(tempFolder.createFile("file_27-4.txt", "27-4 file content"))
            webClient.uploadFile(tempFolder.createFile("file_27-4_2.txt", "27-4-2 file content"), "27-4_dir")
            assertThat(webClient.submit(submission, TSV)).isSuccessful()

            // Verify that the submission files are located in the private and not the public directory
            val submitted = submissionRepository.getExtByAccNo("S-RELEASE004")
            assertThat(File("$ftpPath/${submitted.relPath}")).doesNotExist()

            val key = submitted.secretKey
            val privateFilesPath = "$submissionPath/${key.take(2)}/${key.substring(2)}/${submitted.relPath}/Files"
            val expectedFile = File("$privateFilesPath/file_27-4.txt")
            assertThat(expectedFile).exists()
            assertThat(expectedFile).hasContent("27-4 file content")

            val expectedInnerFile = File("$privateFilesPath/27-4_dir/file_27-4_2.txt")
            assertThat(expectedInnerFile).exists()
            assertThat(expectedInnerFile).hasContent("27-4-2 file content")

            val submission2 =
                tsv {
                    line("Submission", "S-RELEASE004")
                    line("Title", "Submission")
                    line("ReleaseDate", OffsetDateTime.now().toStringDate())
                    line()

                    line("Study")
                    line()

                    line("File", "file_27-4.txt")
                    line()

                    line("File", "27-4_dir")
                    line()
                }.toString()
            val result =
                webClient.submit(
                    submission2,
                    TSV,
                    SubmitParameters(preferredSources = listOf(PreferredSource.SUBMISSION)),
                )
            assertThat(result).isSuccessful()

            // Verify that the submission files are located in the public and not the private directory
            val released = submissionRepository.getExtByAccNo("S-RELEASE004")
            val publicFilesPath = "$ftpPath/${released.relPath}/Files"
            val expectedReleasedFile = File("$publicFilesPath/file_27-4.txt")
            assertThat(expectedReleasedFile).exists()
            assertThat(expectedReleasedFile).hasContent("27-4 file content")

            val expectedReleasedInnerFile = File("$publicFilesPath/27-4_dir/file_27-4_2.txt")
            assertThat(expectedReleasedInnerFile).exists()
            assertThat(expectedReleasedInnerFile).hasContent("27-4-2 file content")

            assertThat(File("$privateFilesPath/file_27-4.txt")).doesNotExist()
            assertThat(File("$privateFilesPath/27-4_dir/file_27-4_2.txt")).doesNotExist()
        }

    @Test
    fun `27-5 release submission using the release operation`() =
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

            webClient.submit(submission, TSV)

            val submitted = submissionRepository.getExtByAccNo(accNo)
            assertThat(submitted.releaseTime).isEqualTo(releaseTime.atMidnight())
            assertThat(submitted.released).isEqualTo(false)

            val (rqtAccNo, rqtVersion) = webClient.releaseSubmission(accNo, newRelease.toInstant())

            waitUntil(timeout = Duration.ofSeconds(10)) {
                submissionRepository.existByAccNoAndVersion(
                    rqtAccNo,
                    rqtVersion,
                )
            }
            val newVersion = submissionRepository.getExtByAccNo(accNo)
            assertThat(newVersion.releaseTime).isEqualTo(newRelease.atMidnight())
            assertThat(newVersion.released).isEqualTo(true)
        }
}
