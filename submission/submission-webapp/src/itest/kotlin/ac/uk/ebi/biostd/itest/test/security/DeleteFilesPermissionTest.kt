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
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.OffsetDateTime

@Import(FilePersistenceConfig::class)
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
class DeleteFilesPermissionTest(
    @param:Autowired val securityTestService: SecurityTestService,
    @param:Autowired val requestRepository: SubmissionRequestPersistenceService,
    @param:LocalServerPort val serverPort: Int,
) {
    private lateinit var webClient: BioWebClient
    private lateinit var superUserWebClient: BioWebClient

    @BeforeAll
    fun init() =
        runBlocking {
            securityTestService.ensureUserRegistration(SuperUser)
            securityTestService.ensureUserRegistration(RegularUser)

            webClient = getWebClient(serverPort, RegularUser)
            superUserWebClient = getWebClient(serverPort, SuperUser)

            securityTestService.ensureSequence("S-BSST")
        }

    @Test
    fun `1-14 Regular user deletes their own public submission files`() =
        runTest {
            val version1 =
                tsv {
                    line("Submission")
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

            val accNo = webClient.submitAndGetAccNo(version1)
            val version2 =
                tsv {
                    line("Submission", accNo)
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

            waitUntil(timeout = TWO_SECONDS) { requestRepository.getRequest(accNo, 2).status == INVALID }
        }

    @Test
    fun `1-15 Regular user with DELETE_FILES permission deletes their own public submission files`() =
        runTest {
            val version1 =
                tsv {
                    line("Submission")
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

            val accNo = webClient.submitAndGetAccNo(version1)
            val version2 =
                tsv {
                    line("Submission", accNo)
                    line("Title", "Update Submission Files")
                    line("ReleaseDate", OffsetDateTime.now().toStringDate())
                    line()
                    line("Study")
                    line()
                    line("File", "file_5-7-1.txt")
                    line()
                }.toString()

            superUserWebClient.grantPermission(RegularUser.email, accNo, AccessType.DELETE_FILES.name)
            webClient.uploadFile(tempFolder.createOrReplaceFile("file_5-7-1.txt", "5-7-1 file updated content"))

            assertThat(webClient.submit(version2, TSV)).isSuccessful()
        }

    @Test
    fun `1-16 Regular user deletes their own public submission filelist files`() =
        runTest {
            val version1 =
                tsv {
                    line("Submission")
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

            val accNo = webClient.submitAndGetAccNo(version1)
            val version2 =
                tsv {
                    line("Submission", accNo)
                    line("Title", "Remove File List")
                    line("ReleaseDate", OffsetDateTime.now().toStringDate())
                    line()
                    line("Study")
                    line()
                    line("File", "file_5-8-1.txt")
                    line()
                }.toString()

            webClient.submitAsync(version2, TSV)

            waitUntil(timeout = TWO_SECONDS) { requestRepository.getRequest(accNo, 2).status == INVALID }
        }

    @Test
    fun `1-17 Collection ADMIN user deletes public submission files`() =
        runTest {
            val collection =
                tsv {
                    line("Submission", "Test-Delete-Collection")
                    line("AccNoTemplate", "!{S-DEL-COL}")
                    line("ReleaseDate", OffsetDateTime.now().toStringDate())
                    line()

                    line("Project")
                }.toString()

            val version1 =
                tsv {
                    line("Submission")
                    line("Title", "Test For Admin")
                    line("AttachTo", "Test-Delete-Collection")
                    line("ReleaseDate", OffsetDateTime.now().toStringDate())
                    line()
                    line("Study")
                    line()
                    line("File", "file_5-17-1.txt")
                    line()
                    line("File", "file_5-17-2.txt")
                    line()
                }.toString()

            assertThat(superUserWebClient.submit(collection, TSV)).isSuccessful()
            superUserWebClient.grantPermission(RegularUser.email, "Test-Delete-Collection", AccessType.ATTACH.name)

            webClient.uploadFile(tempFolder.createFile("file_5-17-1.txt", "5-17-1 file content"))
            webClient.uploadFile(tempFolder.createFile("file_5-17-2.txt", "5-17-2 file content"))

            val accNo = webClient.submitAndGetAccNo(version1)
            val version2 =
                tsv {
                    line("Submission", accNo)
                    line("Title", "Admin Update Submission Files")
                    line("AttachTo", "Test-Delete-Collection")
                    line("ReleaseDate", OffsetDateTime.now().toStringDate())
                    line()
                    line("Study")
                    line()
                    line("File", "file_5-17-1.txt")
                    line()
                }.toString()

            superUserWebClient.grantPermission(RegularUser.email, "Test-Delete-Collection", AccessType.ADMIN.name)
            webClient.uploadFile(tempFolder.createOrReplaceFile("file_5-17-1.txt", "5-17-1 file updated content"))

            assertThat(webClient.submit(version2, TSV)).isSuccessful()
        }

    @Nested
    @SpringBootTest(webEnvironment = RANDOM_PORT, properties = ["app.security.preventFileDeletion=false"])
    inner class PreventFileDeletionFlag(
        @param:Autowired val securityTestService: SecurityTestService,
        @param:LocalServerPort val serverPort: Int,
    ) {
        @Test
        fun `1-18 Regular user deletes their own public submission files when preventFileDeletion is disabled`() =
            runTest {
                val version1 =
                    tsv {
                        line("Submission")
                        line("ReleaseDate", OffsetDateTime.now().toStringDate())
                        line()
                        line("Study")
                        line("File", "file_abc.txt")
                    }.toString()

                webClient.uploadFile(tempFolder.createFile("file_abc.txt", "abc content"))

                val accNo = webClient.submitAndGetAccNo(version1)
                val version2 =
                    tsv {
                        line("Submission", accNo)
                        line("ReleaseDate", OffsetDateTime.now().toStringDate())
                        line()
                        line("Study")
                        line("File", "file_abc.txt")
                    }.toString()
                assertThat(webClient.submit(version2, TSV)).isSuccessful()
            }
    }

    private suspend fun BioWebClient.submitAndGetAccNo(submission: String): String {
        val response = submit(submission, TSV)
        assertThat(response).isSuccessful()

        return response.body.accNo
    }
}
