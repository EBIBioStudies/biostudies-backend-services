package ac.uk.ebi.biostd.itest.test.submission.submit

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.itest.ITestListener
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.ftpPath
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.storageMode
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
import ebi.ac.uk.io.ext.createDirectory
import ebi.ac.uk.io.ext.createFile
import ebi.ac.uk.io.sources.PreferredSource
import ebi.ac.uk.io.sources.PreferredSource.USER_SPACE
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
                }.toString()

            webClient.uploadFile(ITestListener.tempFolder.createFile("file_27-3.txt", "27-3 file content"))
            assertThat(webClient.submit(submission, TSV)).isSuccessful()

            val submitted = submissionRepository.getExtByAccNo("S-RELEASE003")

            val expectedFile = File("$ftpPath/${submitted.relPath}/Files/file_27-3.txt")
            assertThat(expectedFile).exists()
            assertThat(expectedFile).hasContent("27-3 file content")

            val key = submitted.secretKey
            val subFilesPath =
                "$submissionPath/${key.take(2)}/${key.substring(2)}/${submitted.relPath}/Files"
            assertThat(File("$subFilesPath/file_27-3.txt")).doesNotExist()
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
                }.toString()

            webClient.uploadFile(ITestListener.tempFolder.createFile("file_27-4.txt", "27-4 file content"))
            assertThat(webClient.submit(submission, TSV)).isSuccessful()

            // Verify that the submission files are located in the private and not the public directory
            val submitted = submissionRepository.getExtByAccNo("S-RELEASE004")
            assertThat(File("$ftpPath/${submitted.relPath}")).doesNotExist()

            val key = submitted.secretKey
            val privateFilesPath = "$submissionPath/${key.take(2)}/${key.substring(2)}/${submitted.relPath}/Files"
            val expectedFile = File("$privateFilesPath/file_27-4.txt")
            assertThat(expectedFile).exists()
            assertThat(expectedFile).hasContent("27-4 file content")

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

            val privateFile = File("$privateFilesPath/file_27-4.txt")
            assertThat(privateFile).doesNotExist()
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

    @Test
    @EnabledIfSystemProperty(named = "enableFire", matches = "false")
    fun `27-6 file list with directory references`() =
        runTest {
            val dir = tempFolder.createDirectory("27-6_dir")
            val file1 = tempFolder.createFile("file_27-6_1.txt", "27-6 file content 1")
            val file2 = dir.createFile("file_27-6_2.txt", "27-6 file content 2")
            val submission =
                tsv {
                    line("Submission", "S-RELEASE006")
                    line("Title", "Submission With Directory References")
                    line("ReleaseDate", "2099-09-21")
                    line()

                    line("Study")
                    line("File List", "27-6_file-list.tsv")
                    line()
                }.toString()

            val fileListV1 =
                tempFolder.createFile(
                    "27-6_file-list.tsv",
                    tsv {
                        line("Files", "GEN")
                        line("file_27-6_1.txt", "ABC")
                        line("27-6_dir/file_27-6_2.txt", "DEF")
                        line("27-6_dir", "GHI")
                    }.toString(),
                )

            fun assertSubFiles() {
                val extSub = webClient.getExtByAccNo("S-RELEASE006")
                val key = extSub.secretKey
                val privateFilesPath = "$submissionPath/${key.take(2)}/${key.substring(2)}/${extSub.relPath}/Files"

                val fileListFile1 = File("$privateFilesPath/file_27-6_1.txt")
                assertThat(fileListFile1).exists()
                assertThat(fileListFile1).hasContent("27-6 file content 1")

                val fileListFile2 = File("$privateFilesPath/27-6_dir/file_27-6_2.txt")
                assertThat(fileListFile2).exists()
                assertThat(fileListFile2).hasContent("27-6 file content 2")
            }

            webClient.uploadFile(file2, "27-6_dir")
            webClient.uploadFiles(listOf(file1, fileListV1))
            val params = SubmitParameters(storageMode = storageMode, preferredSources = listOf(USER_SPACE))
            assertThat(webClient.submit(submission, TSV, params)).isSuccessful()
            assertSubFiles()

            val fileListV2 =
                tempFolder.createFile(
                    "27-6_file-list.tsv",
                    tsv {
                        line("Files", "GEN")
                        line("file_27-6_1.txt", "ABC")
                        line("27-6_dir/file_27-6_2.txt", "DEF")
                    }.toString(),
                )

            webClient.uploadFiles(listOf(file1, fileListV2))
            assertThat(webClient.submit(submission, TSV, params)).isSuccessful()
            assertSubFiles()
        }
}
