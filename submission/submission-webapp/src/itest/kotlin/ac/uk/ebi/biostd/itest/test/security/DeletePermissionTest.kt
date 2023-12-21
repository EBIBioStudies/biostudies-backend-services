package ac.uk.ebi.biostd.itest.test.security

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.client.integration.web.SubmissionFilesConfig
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.RegularUser
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.storageMode
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.tempFolder
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ac.uk.ebi.biostd.itest.test.security.SubmitPermissionTest.ExistingUser
import ac.uk.ebi.biostd.persistence.common.model.AccessType.ADMIN
import ac.uk.ebi.biostd.persistence.common.model.AccessType.DELETE
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.submission.config.FilePersistenceConfig
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.io.ext.createFile
import ebi.ac.uk.util.date.toStringDate
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
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
class DeletePermissionTest(
    @Autowired private val securityTestService: SecurityTestService,
    @Autowired private val submissionRepository: SubmissionPersistenceQueryService,
    @LocalServerPort val serverPort: Int,
) {
    private lateinit var superUserWebClient: BioWebClient
    private lateinit var regularUserWebClient: BioWebClient
    private lateinit var existingUserWebClient: BioWebClient

    @BeforeAll
    fun init() = runBlocking {
        setUpTestUsers()
        setUpTestCollection()
    }

    @Test
    fun `1-1 superuser deletes private submission`() = runTest {
        val submission = tsv {
            line("Submission", "S-DLT1")
            line("Title", "Delete Submission 1")
            line()
        }.toString()

        val onBehalfClient = SecurityWebClient
            .create("http://localhost:$serverPort")
            .getAuthenticatedClient(SuperUser.email, SuperUser.password, RegularUser.email)

        assertThat(onBehalfClient.submitSingle(submission, TSV)).isSuccessful()

        assertThatExceptionOfType(WebClientException::class.java)
            .isThrownBy { superUserWebClient.deleteSubmission("S-DLT1") }
            .withMessageContaining("The user biostudies-mgmt@ebi.ac.uk is not allowed to delete the submission S-DLT1")
    }

    @Test
    fun `1-2 superuser deletes public submission`() = runTest {
        val submission = tsv {
            line("Submission", "S-DLT2")
            line("Title", "Delete Submission 2")
            line("ReleaseDate", OffsetDateTime.now().toStringDate())
            line()
        }.toString()

        val onBehalfClient = SecurityWebClient
            .create("http://localhost:$serverPort")
            .getAuthenticatedClient(SuperUser.email, SuperUser.password, RegularUser.email)

        assertThat(onBehalfClient.submitSingle(submission, TSV)).isSuccessful()

        assertThatExceptionOfType(WebClientException::class.java)
            .isThrownBy { superUserWebClient.deleteSubmission("S-DLT2") }
            .withMessageContaining("The user biostudies-mgmt@ebi.ac.uk is not allowed to delete the submission S-DLT2")
    }

    @Test
    fun `1-3 regular user deletes private submission`() {
        val submission = tsv {
            line("Submission", "S-DLT3")
            line("Title", "Delete Submission 3")
            line()
        }.toString()

        assertThat(superUserWebClient.submitSingle(submission, TSV)).isSuccessful()

        assertThatExceptionOfType(WebClientException::class.java)
            .isThrownBy { regularUserWebClient.deleteSubmission("S-DLT3") }
            .withMessageContaining("The user regular@ebi.ac.uk is not allowed to delete the submission S-DLT3")
    }

    @Test
    fun `1-4 regular user deletes public submission`() {
        val submission = tsv {
            line("Submission", "S-DLT4")
            line("Title", "Delete Submission 4")
            line("ReleaseDate", OffsetDateTime.now().toStringDate())
            line()
        }.toString()

        assertThat(superUserWebClient.submitSingle(submission, TSV)).isSuccessful()

        assertThatExceptionOfType(WebClientException::class.java)
            .isThrownBy { regularUserWebClient.deleteSubmission("S-DLT4") }
            .withMessageContaining("The user regular@ebi.ac.uk is not allowed to delete the submission S-DLT4")
    }

    @Test
    fun `1-5 regular user with access tag permission deletes private submission`() = runTest {
        val submission = tsv {
            line("Submission", "S-DLT5")
            line("Title", "Delete Submission 5")
            line("AttachTo", "ACollection")
            line()
        }.toString()

        assertThat(superUserWebClient.submitSingle(submission, TSV)).isSuccessful()
        superUserWebClient.givePermissionToUser(RegularUser.email, "ACollection", DELETE.name)

        regularUserWebClient.deleteSubmission("S-DLT5")
        assertDeletedSubmission("S-DLT5")
    }

    @Test
    fun `1-6 regular user with access tag permission deletes public submission`() = runTest {
        val submission = tsv {
            line("Submission", "S-DLT6")
            line("Title", "Simple Submission")
            line("AttachTo", "ACollection")
            line("ReleaseDate", OffsetDateTime.now().toStringDate())
            line()
        }.toString()

        assertThat(superUserWebClient.submitSingle(submission, TSV)).isSuccessful()
        superUserWebClient.givePermissionToUser(RegularUser.email, "ACollection", DELETE.name)
        regularUserWebClient.deleteSubmission("S-DLT6")
        assertDeletedSubmission("S-DLT6")
    }

    @Test
    fun `1-7 regular user with admin persmission deletes private submission`() = runTest {
        val submission = tsv {
            line("Submission", "S-DLT7")
            line("Title", "Delete Submission 7")
            line("AttachTo", "ACollection")
            line()
        }.toString()

        assertThat(superUserWebClient.submitSingle(submission, TSV)).isSuccessful()
        superUserWebClient.givePermissionToUser(ExistingUser.email, "ACollection", ADMIN.name)

        assertThatExceptionOfType(WebClientException::class.java)
            .isThrownBy { existingUserWebClient.deleteSubmission("S-DLT7") }
            .withMessageContaining("The user register_user@ebi.ac.uk is not allowed to delete the submission S-DLT7")
    }

    @Test
    fun `1-8 regular user with admin persmission deletes public submission`() = runTest {
        val submission = tsv {
            line("Submission", "S-DLT8")
            line("Title", "Delete Submission 8")
            line("AttachTo", "ACollection")
            line("ReleaseDate", OffsetDateTime.now().toStringDate())
            line()
        }.toString()

        assertThat(superUserWebClient.submitSingle(submission, TSV)).isSuccessful()
        superUserWebClient.givePermissionToUser(ExistingUser.email, "ACollection", ADMIN.name)

        assertThatExceptionOfType(WebClientException::class.java)
            .isThrownBy { existingUserWebClient.deleteSubmission("S-DLT8") }
            .withMessageContaining("The user register_user@ebi.ac.uk is not allowed to delete the submission S-DLT8")
    }

    @Test
    fun `1-9 delete own public submission`() {
        val submission = tsv {
            line("Submission", "S-DLT9")
            line("Title", "Delete Submission 9")
            line("ReleaseDate", OffsetDateTime.now().toStringDate())
            line()
        }.toString()

        val onBehalfClient = SecurityWebClient
            .create("http://localhost:$serverPort")
            .getAuthenticatedClient(SuperUser.email, SuperUser.password, RegularUser.email)

        assertThat(onBehalfClient.submitSingle(submission, TSV)).isSuccessful()

        assertThatExceptionOfType(WebClientException::class.java)
            .isThrownBy { regularUserWebClient.deleteSubmission("S-DLT9") }
            .withMessageContaining("The user regular@ebi.ac.uk is not allowed to delete the submission S-DLT9")
    }

    @Test
    fun `1-10 delete own private submission`() = runTest {
        val submission = tsv {
            line("Submission", "S-DLT10")
            line("Title", "Delete Submission 10")
            line()
        }.toString()

        val onBehalfClient = SecurityWebClient
            .create("http://localhost:$serverPort")
            .getAuthenticatedClient(SuperUser.email, SuperUser.password, RegularUser.email)

        assertThat(onBehalfClient.submitSingle(submission, TSV)).isSuccessful()
        regularUserWebClient.deleteSubmission("S-DLT10")
        assertDeletedSubmission("S-DLT10")
    }

    @Test
    fun `1-11 delete own public subsmissions`() = runTest {
        val submission1 = tsv {
            line("Submission", "S-DLT111")
            line("Title", "Delete Submission 111")
            line("ReleaseDate", OffsetDateTime.now().toStringDate())
            line()
        }.toString()
        val submission2 = tsv {
            line("Submission", "S-DLT112")
            line("Title", "Delete Submission 112")
            line()
        }.toString()
        val submission3 = tsv {
            line("Submission", "S-DLT113")
            line("Title", "Delete Submission 113")
            line("ReleaseDate", OffsetDateTime.now().toStringDate())
            line()
        }.toString()

        assertThat(superUserWebClient.submitSingle(submission1, TSV)).isSuccessful()
        assertThat(superUserWebClient.submitSingle(submission2, TSV)).isSuccessful()
        assertThat(superUserWebClient.submitSingle(submission3, TSV)).isSuccessful()

        val errorMsg = "The user biostudies-mgmt@ebi.ac.uk is not allowed to delete the submissions S-DLT111, S-DLT113"
        assertThatExceptionOfType(WebClientException::class.java)
            .isThrownBy { superUserWebClient.deleteSubmissions(listOf("S-DLT111", "S-DLT112", "S-DLT113")) }
            .withMessageContaining(errorMsg)
    }

    @Test
    fun `1-12 delete own private subsmissions`() = runTest {
        val submission1 = tsv {
            line("Submission", "S-DLT121")
            line("Title", "Delete Submission 121")
            line()
        }.toString()
        val submission2 = tsv {
            line("Submission", "S-DLT122")
            line("Title", "Delete Submission 122")
            line()
        }.toString()

        assertThat(superUserWebClient.submitSingle(submission1, TSV)).isSuccessful()
        assertThat(superUserWebClient.submitSingle(submission2, TSV)).isSuccessful()

        superUserWebClient.deleteSubmissions(listOf("S-DLT121", "S-DLT122"))
        Thread.sleep(5000)

        assertDeletedSubmission("S-DLT121")
        assertDeletedSubmission("S-DLT122")
    }

    @Test
    fun `1-13 resubmit deleted submission`() = runTest {
        val submission = tsv {
            line("Submission", "S-DLT13")
            line("Title", "Delete Submission 13")
            line()
        }.toString()

        superUserWebClient.submitSingle(submission, TSV)
        superUserWebClient.deleteSubmission("S-DLT13")
        superUserWebClient.submitSingle(submission, TSV)

        val resubmitted = submissionRepository.getExtByAccNo("S-DLT13")
        assertThat(resubmitted.version).isEqualTo(2)
    }

    private suspend fun assertDeletedSubmission(accNo: String, version: Int = -1) {
        val deletedSubmission = submissionRepository.getExtByAccNoAndVersion(accNo, version)
        assertThat(deletedSubmission.version).isEqualTo(version)
    }

    private fun setUpTestCollection() {
        val project = tsv {
            line("Submission", "ACollection")
            line("AccNoTemplate", "!{S-APR}")
            line()

            line("Project")
        }.toString()
        val projectFile = tempFolder.createFile("a-collection.tsv", project)

        val filesConfig = SubmissionFilesConfig(emptyList(), storageMode)
        assertThat(superUserWebClient.submitSingle(projectFile, filesConfig)).isSuccessful()
    }

    private suspend fun setUpTestUsers() {
        securityTestService.ensureUserRegistration(SuperUser)
        securityTestService.ensureUserRegistration(RegularUser)
        securityTestService.ensureUserRegistration(ExistingUser)

        superUserWebClient = getWebClient(serverPort, SuperUser)
        regularUserWebClient = getWebClient(serverPort, RegularUser)
        existingUserWebClient = getWebClient(serverPort, ExistingUser)
    }
}
