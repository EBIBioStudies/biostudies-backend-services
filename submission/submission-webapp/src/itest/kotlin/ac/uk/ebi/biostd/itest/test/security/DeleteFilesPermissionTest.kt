package ac.uk.ebi.biostd.itest.test.security

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.RegularUser
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.tempFolder
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ac.uk.ebi.biostd.persistence.common.model.AccessType
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.submission.config.FilePersistenceConfig
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.coroutines.waitUntil
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.io.ext.createFile
import ebi.ac.uk.io.ext.createOrReplaceFile
import ebi.ac.uk.model.RequestStatus.INVALID
import ebi.ac.uk.util.date.toStringDate
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.awaitility.Durations.TWO_SECONDS
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.OffsetDateTime

@Import(FilePersistenceConfig::class)
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DeleteFilesPermissionTest(
    @Autowired val securityTestService: SecurityTestService,
    @Autowired val requestRepository: SubmissionRequestPersistenceService,
    @LocalServerPort val serverPort: Int,
) {
    private lateinit var webClient: BioWebClient

    @BeforeAll
    fun init() =
        runBlocking {
            securityTestService.ensureUserRegistration(SuperUser)
            securityTestService.ensureUserRegistration(RegularUser)
            webClient = getWebClient(serverPort, SuperUser)
        }

    @Test
    fun `1-14 Regular user deletes their own public submission files`() =
        runTest {
            val version1 =
                tsv {
                    line("Submission", "S-RSTST6")
                    line("Title", "Update Submission Files")
                    line("ReleaseDate", OffsetDateTime.now().toStringDate())
                    line()
                    line("Study")
                    line("File List", "file-list_5-6.tsv")
                    line()
                    line("File", "file_5-6-1.txt")
                    line()
                }.toString()

            val fileListVersion1 =
                tsv {
                    line("Files", "Type")
                    line("file_5-6-2.txt", "Referenced")
                    line()
                }.toString()

            webClient.uploadFile(tempFolder.createFile("file-list_5-6.tsv", fileListVersion1))
            webClient.uploadFile(tempFolder.createFile("file_5-6-1.txt", "5-6-1 file content"))
            webClient.uploadFile(tempFolder.createFile("file_5-6-2.txt", "5-6-2 file content"))
            assertThat(webClient.submit(version1, TSV)).isSuccessful()

            val version2 =
                tsv {
                    line("Submission", "S-RSTST6")
                    line("Title", "Update Submission Files")
                    line("ReleaseDate", OffsetDateTime.now().toStringDate())
                    line()
                    line("Study")
                    line("File List", "file-list_5-6.tsv")
                    line()
                    line("File", "file_5-6-1.txt")
                    line()
                }.toString()

            webClient.uploadFile(tempFolder.createOrReplaceFile("file_5-6-1.txt", "5-6-1 file updated content"))
            webClient.uploadFile(tempFolder.createOrReplaceFile("file_5-6-2.txt", "5-6-2 file updated content"))
            webClient.submitAsync(version2, TSV)

            waitUntil(timeout = TWO_SECONDS) { requestRepository.getRequestStatus("S-RSTST6", 2) == INVALID }
        }

    @Test
    fun `1-15  Regular user with UPDATE_PUBLIC permission deletes their own public submission files`() =
        runTest {
            val version1 =
                tsv {
                    line("Submission", "S-RSTST7")
                    line("Title", "Update Submission Files")
                    line("ReleaseDate", OffsetDateTime.now().toStringDate())
                    line()
                    line("Study")
                    line()
                    line("File", "file_5-7-1.txt")
                    line()
                    line("File", "file_5-7-2.txt")
                    line()
                }.toString()

            webClient.uploadFile(tempFolder.createFile("file_5-7-1.txt", "5-7-1 file content"))
            webClient.uploadFile(tempFolder.createFile("file_5-7-2.txt", "5-7-2 file content"))
            assertThat(webClient.submit(version1, TSV)).isSuccessful()

            val version2 =
                tsv {
                    line("Submission", "S-RSTST7")
                    line("Title", "Update Submission Files")
                    line("ReleaseDate", OffsetDateTime.now().toStringDate())
                    line()
                    line("Study")
                    line()
                    line("File", "file_5-7-1.txt")
                    line()
                }.toString()

            webClient.grantPermission(SuperUser.email, "S-RSTST7", AccessType.DELETE.name)
            webClient.uploadFile(tempFolder.createOrReplaceFile("file_5-7-1.txt", "5-7-1 file updated content"))

            assertThat(webClient.submit(version2, TSV)).isSuccessful()
        }

    @Test
    fun `1-16 Regular user deletes their own public submission filelist files`() =
        runTest {
            val version1 =
                tsv {
                    line("Submission", "S-RSTST8")
                    line("Title", "Remove File List")
                    line("ReleaseDate", OffsetDateTime.now().toStringDate())
                    line()
                    line("Study")
                    line("File List", "file-list_5-8.tsv")
                    line()
                    line("File", "file_5-8-1.txt")
                    line()
                }.toString()

            val fileListVersion1 =
                tsv {
                    line("Files", "Type")
                    line("file_5-8-2.txt", "Referenced")
                    line()
                }.toString()

            webClient.uploadFile(tempFolder.createFile("file-list_5-8.tsv", fileListVersion1))
            webClient.uploadFile(tempFolder.createFile("file_5-8-1.txt", "5-8-1 file content"))
            webClient.uploadFile(tempFolder.createFile("file_5-8-2.txt", "5-8-2 file content"))
            assertThat(webClient.submit(version1, TSV)).isSuccessful()

            val version2 =
                tsv {
                    line("Submission", "S-RSTST8")
                    line("Title", "Remove File List")
                    line("ReleaseDate", OffsetDateTime.now().toStringDate())
                    line()
                    line("Study")
                    line()
                    line("File", "file_5-8-1.txt")
                    line()
                }.toString()

            webClient.submitAsync(version2, TSV)

            waitUntil(timeout = TWO_SECONDS) { requestRepository.getRequestStatus("S-RSTST8", 2) == INVALID }
        }
}
