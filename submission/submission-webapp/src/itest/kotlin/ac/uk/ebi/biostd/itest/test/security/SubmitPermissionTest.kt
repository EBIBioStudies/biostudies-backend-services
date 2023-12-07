package ac.uk.ebi.biostd.itest.test.security

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient.Companion.create
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.entities.TestUser
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ac.uk.ebi.biostd.persistence.common.model.AccessType.ADMIN
import ac.uk.ebi.biostd.persistence.common.model.AccessType.ATTACH
import ac.uk.ebi.biostd.submission.config.FilePersistenceConfig
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension

@Import(FilePersistenceConfig::class)
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SubmitPermissionTest(
    @Autowired private val securityTestService: SecurityTestService,
    @LocalServerPort val serverPort: Int,
) {
    private val collection = tsv {
        line("Submission", "ACollection")
        line("AccNoTemplate", "!{S-APR}")
        line()

        line("Project")
    }.toString()

    private lateinit var superUserWebClient: BioWebClient
    private lateinit var regularUserWebClient: BioWebClient

    @BeforeAll
    fun init() = runBlocking {
        securityTestService.ensureUserRegistration(SuperUser)
        securityTestService.ensureUserRegistration(ExistingUser)
        securityTestService.ensureUserRegistration(ImpersonatedUser)

        superUserWebClient = getWebClient(serverPort, SuperUser)
        regularUserWebClient = getWebClient(serverPort, ExistingUser)
    }

    @Test
    fun `4-1 create collection with superuser`() {
        assertThat(superUserWebClient.submitSingle(collection, SubmissionFormat.TSV)).isSuccessful()
    }

    @Test
    fun `4-2 create collection with regular user`() {
        assertThatExceptionOfType(WebClientException::class.java).isThrownBy {
            regularUserWebClient.submitSingle(collection, SubmissionFormat.TSV)
        }
    }

    @Test
    fun `4-3 submit without attach permission`() {
        val project = tsv {
            line("Submission", "TestCollection")
            line("AccNoTemplate", "!{S-CLL}")
            line()

            line("Project")
        }.toString()

        val submission = tsv {
            line("Submission")
            line("AttachTo", "TestCollection")
            line("Title", "Test Submission")
        }.toString()

        assertThat(superUserWebClient.submitSingle(project, SubmissionFormat.TSV)).isSuccessful()
        assertThatExceptionOfType(WebClientException::class.java)
            .isThrownBy { regularUserWebClient.submitSingle(submission, SubmissionFormat.TSV) }
            .withMessageContaining(
                "The user register_user@ebi.ac.uk is not allowed to submit to TestCollection collection"
            )
    }

    @Test
    fun `4-4 submit with attach permission access tag`() {
        val project = tsv {
            line("Submission", "TestCollection2")
            line("AccNoTemplate", "!{S-COLL}")
            line()

            line("Project")
        }.toString()

        val submission = tsv {
            line("Submission")
            line("AttachTo", "TestCollection2")
            line("Title", "Test Submission")
        }.toString()

        assertThat(superUserWebClient.submitSingle(project, SubmissionFormat.TSV)).isSuccessful()
        setAttachPermission(ExistingUser, "TestCollection2")

        assertThat(regularUserWebClient.submitSingle(submission, SubmissionFormat.TSV)).isSuccessful()
    }

    @Test
    fun `4-5 registering user and submit to default project`() {
        val project = tsv {
            line("Submission", "TestCollection3")
            line("AccNoTemplate", "!{S-CLC}")
            line()

            line("Project")
        }.toString()

        val submission = tsv {
            line("Submission")
            line("AttachTo", "TestCollection3")
            line("Title", "Test Submission")
        }.toString()

        create("http://localhost:$serverPort").registerUser(NewUser.asRegisterRequest())
        assertThat(superUserWebClient.submitSingle(project, SubmissionFormat.TSV)).isSuccessful()

        setAttachPermission(NewUser, "TestCollection3")
        assertThat(getWebClient(serverPort, NewUser).submitSingle(submission, SubmissionFormat.TSV)).isSuccessful()
    }

    @Test
    fun `4-6 submit with collection admin permission`() {
        val project = tsv {
            line("Submission", "TestCollection4")
            line("AccNoTemplate", "!{S-CLCT}")
            line()

            line("Project")
        }.toString()

        val submission = tsv {
            line("Submission")
            line("AttachTo", "TestCollection4")
            line("Title", "Test Submission")
        }.toString()

        assertThat(superUserWebClient.submitSingle(project, SubmissionFormat.TSV)).isSuccessful()
        superUserWebClient.givePermissionToUser(ExistingUser.email, "TestCollection4", ADMIN.name)

        assertThat(regularUserWebClient.submitSingle(submission, SubmissionFormat.TSV)).isSuccessful()
    }

    @Test
    fun `4-7 resubmit with collection admin permission`() {
        val project = tsv {
            line("Submission", "TestCollection5")
            line("AccNoTemplate", "!{S-TCLT}")
            line()

            line("Project")
        }.toString()

        val submission = tsv {
            line("Submission")
            line("AttachTo", "TestCollection5")
            line("Title", "Test Submission")
        }.toString()

        assertThat(superUserWebClient.submitSingle(project, SubmissionFormat.TSV)).isSuccessful()
        superUserWebClient.givePermissionToUser(ExistingUser.email, "TestCollection5", ADMIN.name)

        assertThat(regularUserWebClient.submitSingle(submission, SubmissionFormat.TSV)).isSuccessful()

        val resubmission = tsv {
            line("Submission", "S-TCLT1")
            line("AttachTo", "TestCollection5")
            line("Title", "Test Resubmission")
        }.toString()

        assertThat(regularUserWebClient.submitSingle(resubmission, SubmissionFormat.TSV)).isSuccessful()
    }

    @Test
    fun `4-8 owner resubmits without attach permission`() {
        val project = tsv {
            line("Submission", "TestCollection6")
            line("AccNoTemplate", "!{T-CLLC}")
            line()

            line("Project")
        }.toString()

        val submission = tsv {
            line("Submission")
            line("AttachTo", "TestCollection6")
            line("Title", "Test Submission")
        }.toString()

        val impersonatedUserClient = getWebClient(serverPort, ImpersonatedUser)
        val onBehalfClient = getWebClient(serverPort, SuperUser, ImpersonatedUser)

        assertThat(superUserWebClient.submitSingle(project, SubmissionFormat.TSV)).isSuccessful()
        assertThat(onBehalfClient.submitSingle(submission, SubmissionFormat.TSV)).isSuccessful()

        val resubmission = tsv {
            line("Submission", "T-CLLC1")
            line("AttachTo", "TestCollection6")
            line("Title", "Test Resubmission")
        }.toString()
        assertThat(impersonatedUserClient.submitSingle(resubmission, SubmissionFormat.TSV)).isSuccessful()
    }

    private fun setAttachPermission(testUser: TestUser, collection: String) =
        superUserWebClient.givePermissionToUser(testUser.email, collection, ATTACH.name)

    object ExistingUser : TestUser {
        override val username = "Register User"
        override val email = "register_user@ebi.ac.uk"
        override val password = "1234"
        override val superUser = false
    }

    object NewUser : TestUser {
        override val username = "New User"
        override val email = "new_user@ebi.ac.uk"
        override val password = "1234"
        override val superUser = false
    }

    object ImpersonatedUser : TestUser {
        override val username = "Impersonated User"
        override val email = "impersonated_user@ebi.ac.uk"
        override val password = "1234"
        override val superUser = false
    }
}
