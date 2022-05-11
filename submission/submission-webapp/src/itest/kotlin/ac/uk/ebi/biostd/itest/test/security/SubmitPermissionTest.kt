package ac.uk.ebi.biostd.itest.test.security

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient.Companion.create
import ac.uk.ebi.biostd.common.config.PersistenceConfig
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.entities.TestUser
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ac.uk.ebi.biostd.persistence.common.model.AccessType.ATTACH
import ac.uk.ebi.biostd.persistence.model.DbAccessPermission
import ac.uk.ebi.biostd.persistence.repositories.AccessPermissionRepository
import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepo
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension

@Import(PersistenceConfig::class)
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SubmitPermissionTest(
    @Autowired private val securityTestService: SecurityTestService,
    @Autowired private val userDataRepository: UserDataRepository,
    @Autowired private val tagsDataRepository: AccessTagDataRepo,
    @Autowired private val accessPermissionRepository: AccessPermissionRepository,
    @LocalServerPort val serverPort: Int,
) {

    private val project = tsv {
        line("Submission", "AProject")
        line("AccNoTemplate", "!{S-APR}")
        line()

        line("Project")
    }.toString()

    private lateinit var superUserWebClient: BioWebClient
    private lateinit var regularUserWebClient: BioWebClient

    @BeforeAll
    fun init() {
        securityTestService.ensureRegisterUser(SuperUser)
        securityTestService.ensureRegisterUser(ExistingUser)

        superUserWebClient = getWebClient(serverPort, SuperUser)
        regularUserWebClient = getWebClient(serverPort, ExistingUser)
    }

    @Test
    fun `create project with superuser`() {
        assertThat(superUserWebClient.submitSingle(project, SubmissionFormat.TSV)).isSuccessful()
    }

    @Test
    fun `create project with regular user`() {
        assertThatExceptionOfType(WebClientException::class.java).isThrownBy {
            regularUserWebClient.submitSingle(project, SubmissionFormat.TSV)
        }
    }

    @Test
    fun `submit without attach permission`() {
        val project = tsv {
            line("Submission", "TestProject")
            line("AccNoTemplate", "!{S-TPR}")
            line()

            line("Project")
        }.toString()

        val submission = tsv {
            line("Submission")
            line("AttachTo", "TestProject")
            line("Title", "Test Submission")
        }.toString()

        assertThat(superUserWebClient.submitSingle(project, SubmissionFormat.TSV)).isSuccessful()
        assertThatExceptionOfType(WebClientException::class.java)
            .isThrownBy { regularUserWebClient.submitSingle(submission, SubmissionFormat.TSV) }
            .withMessageContaining(
                "The user register_user@ebi.ac.uk is not allowed to submit to TestProject project"
            )
    }

    @Test
    fun `submit with attach permission access tag`() {
        val project = tsv {
            line("Submission", "TestProject2")
            line("AccNoTemplate", "!{S-TPRJ}")
            line()

            line("Project")
        }.toString()

        val submission = tsv {
            line("Submission")
            line("AttachTo", "TestProject2")
            line("Title", "Test Submission")
        }.toString()

        assertThat(superUserWebClient.submitSingle(project, SubmissionFormat.TSV)).isSuccessful()
        setAttachPermission(ExistingUser, "TestProject2")

        assertThat(regularUserWebClient.submitSingle(submission, SubmissionFormat.TSV)).isSuccessful()
    }

    @Test
    fun `registering user and submit to default project`() {
        val project = tsv {
            line("Submission", "TestProject3")
            line("AccNoTemplate", "!{S-PROJ}")
            line()

            line("Project")
        }.toString()

        val submission = tsv {
            line("Submission")
            line("AttachTo", "TestProject3")
            line("Title", "Test Submission")
        }.toString()

        create("http://localhost:$serverPort").registerUser(NewUser.asRegisterRequest())
        assertThat(superUserWebClient.submitSingle(project, SubmissionFormat.TSV)).isSuccessful()

        setAttachPermission(NewUser, "TestProject3")
        assertThat(getWebClient(serverPort, NewUser).submitSingle(submission, SubmissionFormat.TSV)).isSuccessful()
    }

    private fun setAttachPermission(testUser: TestUser, project: String) {
        val accessTag = tagsDataRepository.getByName(project)
        val user = userDataRepository.getByEmailAndActive(testUser.email, active = true)
        val attachPermission = DbAccessPermission(accessType = ATTACH, user = user, accessTag = accessTag)
        accessPermissionRepository.save(attachPermission)
    }

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
}
