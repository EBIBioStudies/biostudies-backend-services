package ac.uk.ebi.biostd.itest.test.user

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.common.config.PersistenceConfig
import ac.uk.ebi.biostd.itest.common.BaseIntegrationTest
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.DefaultUser
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.entities.TestUser
import ac.uk.ebi.biostd.persistence.common.model.AccessType
import ac.uk.ebi.biostd.persistence.common.model.AccessType.ADMIN
import ac.uk.ebi.biostd.persistence.common.model.AccessType.ATTACH
import ac.uk.ebi.biostd.persistence.common.model.AccessType.DELETE
import ac.uk.ebi.biostd.persistence.common.model.AccessType.UPDATE
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.integration.MongoDbReposConfig
import ac.uk.ebi.biostd.persistence.model.DbAccessPermission
import ac.uk.ebi.biostd.persistence.repositories.AccessPermissionRepository
import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepo
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ebi.ac.uk.api.security.RegisterRequest
import ebi.ac.uk.dsl.line
import ebi.ac.uk.dsl.tsv
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import ebi.ac.uk.asserts.assertThat as assertThatSuccess

import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(TemporaryFolderExtension::class)
internal class UserPrivilegesITest(tempFolder: TemporaryFolder) : BaseIntegrationTest(tempFolder) {

    @Nested
    @Import(value = [PersistenceConfig::class, MongoDbReposConfig::class])
    @ExtendWith(SpringExtension::class)
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    inner class UserPrivileges(
        @Autowired val submissionRepository: SubmissionDocDataRepository,
        @Autowired val userDataRepository: UserDataRepository,
        @Autowired val permissionRepository: AccessPermissionRepository,
        @Autowired val accessTagDataRepo: AccessTagDataRepo,
        @Autowired val securityTestService: SecurityTestService
    ) {
        @LocalServerPort
        private var serverPort: Int = 0

        private lateinit var superClient: BioWebClient
        private lateinit var authorClient: BioWebClient
        private lateinit var notAuthorClient: BioWebClient
        private lateinit var defaultClient: BioWebClient

        @BeforeAll
        fun init() {
            securityTestService.registerUser(SuperUser)
            superClient = getWebClient(serverPort, SuperUser)

            securityTestService.registerUser(AuthorUser)
            authorClient = getWebClient(serverPort, AuthorUser)

            securityTestService.registerUser(NotAuthorUser)
            notAuthorClient = getWebClient(serverPort, NotAuthorUser)

            securityTestService.registerUser(DefaultUser)
            defaultClient = getWebClient(serverPort, DefaultUser)

            superClient.submitSingle(testProject, TSV)
            givePermissionToUserOverTestProject(ATTACH, AuthorUser.email)
        }

        @Test
        fun `regular client can not read if do not have permission over the collection READ`() {
        }

        @Test
        fun `regular client can not read if do not have permission over the collection ADMIN`() {
        }

        @Test
        fun `regular client can not attach a submission if is not the owner and do not have permission ATTACH`() {
            assertThatExceptionOfType(WebClientException::class.java)
                .isThrownBy { notAuthorClient.submitSingle(TSVSubmission, TSV) }
                .withMessageContaining(
                    "The user notAuthor@ebi.ac.uk is not allowed to submit to Test-Project project"
                )

            val permission = givePermissionToUserOverTestProject(ATTACH, NotAuthorUser.email)

            val savedSubmission = notAuthorClient.submitSingle(TSVSubmission, TSV)

            permissionRepository.delete(permission)
            superClient.deleteSubmission(savedSubmission.body.accNo)
        }

        @Test
        fun `regular client can not attach a submission if is not the owner and do not have permission ADMIN`() {
            assertThatExceptionOfType(WebClientException::class.java)
                .isThrownBy { notAuthorClient.submitSingle(TSVSubmission, TSV) }
                .withMessageContaining(
                    "The user notAuthor@ebi.ac.uk is not allowed to submit to Test-Project project"
                )

            val permission = givePermissionToUserOverTestProject(ADMIN, NotAuthorUser.email)

            val savedSubmission = notAuthorClient.submitSingle(TSVSubmission, TSV)
            assertThatSuccess(savedSubmission).isSuccessful()
            permissionRepository.delete(permission)
            superClient.deleteSubmission(savedSubmission.body.accNo)
        }

        @Test
        fun `regular client can not update a submission if is not the owner and do not have permission UPDATE`() {
            val permissionDefault = givePermissionToUserOverTestProject(ATTACH, DefaultUser.email)

            val savedSub = defaultClient.submitSingle(TSVSubmission, TSV)
            val submission = tsv {
                line("Submission", savedSub.body.accNo)
                line("AttachTo", "Test-Project")
                line("Title", "Sample Submission")
            }.toString()

            assertThatExceptionOfType(WebClientException::class.java)
                .isThrownBy { notAuthorClient.submitSingle(submission, TSV) }
                .withMessageContaining(
                    "The user {notAuthor@ebi.ac.uk} is not allowed to update the submission ${savedSub.body.accNo}"
                )

            val permission = givePermissionToUserOverTestProject(UPDATE, NotAuthorUser.email)

            assertThatSuccess(notAuthorClient.submitSingle(submission, TSV)).isSuccessful()
            permissionRepository.delete(permission)
            permissionRepository.delete(permissionDefault)
            superClient.deleteSubmission(savedSub.body.accNo)
        }

        @Test
        fun `regular client can not update a submission if is not the owner and do not have permission ADMIN`() {
            val permissionDefault = givePermissionToUserOverTestProject(ATTACH, DefaultUser.email)

            val savedSub = defaultClient.submitSingle(TSVSubmission, TSV)
            val submission = tsv {
                line("Submission", savedSub.body.accNo)
                line("AttachTo", "Test-Project")
                line("Title", "Sample Submission")
            }.toString()

            assertThatExceptionOfType(WebClientException::class.java)
                .isThrownBy { notAuthorClient.submitSingle(submission, TSV) }
                .withMessageContaining(
                    "The user {notAuthor@ebi.ac.uk} is not allowed to update the submission ${savedSub.body.accNo}"
                )

            val permission = givePermissionToUserOverTestProject(ADMIN, NotAuthorUser.email)

            assertThatSuccess(notAuthorClient.submitSingle(submission, TSV)).isSuccessful()
            permissionRepository.delete(permission)
            permissionRepository.delete(permissionDefault)
            superClient.deleteSubmission(savedSub.body.accNo)
        }

        @Test
        fun `regular client can not delete a submission if is not the owner and do not have permission DELETE`() {
            val savedSub = authorClient.submitSingle(TSVSubmission, TSV)

            assertThatExceptionOfType(WebClientException::class.java)
                .isThrownBy { notAuthorClient.deleteSubmission(savedSub.body.accNo) }
                .withMessageContaining(
                    "The user {notAuthor@ebi.ac.uk} is not allowed to delete the submission ${savedSub.body.accNo}"
                )

            val permission = givePermissionToUserOverTestProject(DELETE, NotAuthorUser.email)

            notAuthorClient.deleteSubmission(savedSub.body.accNo)
            permissionRepository.delete(permission)
        }

        @Test
        fun `regular client can not delete a submission if is not the owner and do not have permission ADMIN`() {
            val savedSub = authorClient.submitSingle(TSVSubmission, TSV)

            assertThatExceptionOfType(WebClientException::class.java)
                .isThrownBy { notAuthorClient.deleteSubmission(savedSub.body.accNo) }
                .withMessageContaining(
                    "The user {notAuthor@ebi.ac.uk} is not allowed to delete the submission ${savedSub.body.accNo}"
                )

            val permission = givePermissionToUserOverTestProject(ADMIN, NotAuthorUser.email)

            notAuthorClient.deleteSubmission(savedSub.body.accNo)
            permissionRepository.delete(permission)
        }

        private fun givePermissionToUserOverTestProject(accessType: AccessType, user: String) =
            permissionRepository.save(
                DbAccessPermission(
                    accessType = accessType,
                    user = userDataRepository.getByEmail(user),
                    accessTag = accessTagDataRepo.findByName("Test-Project")!!
                )
            )
    }

    companion object {
        val testProject = tsv {
            line("Submission", "Test-Project")
            line("AccNoTemplate", "!{S-TEST}")
            line()

            line("Project")
        }.toString()
        val TSVSubmission = tsv {
            line("Submission")
            line("AttachTo", "Test-Project")
            line("Title", "Sample Submission")
        }.toString()

        object AuthorUser : TestUser {
            override val username = "Author User"
            override val email = "author@ebi.ac.uk"
            override val password = "123456"
            override val superUser = false

            override fun asRegisterRequest() =
                RegisterRequest(username, email, password)
        }

        object NotAuthorUser : TestUser {
            override val username = "Not Author User"
            override val email = "notAuthor@ebi.ac.uk"
            override val password = "123456"
            override val superUser = false

            override fun asRegisterRequest() =
                RegisterRequest(username, email, password)
        }
    }
}
