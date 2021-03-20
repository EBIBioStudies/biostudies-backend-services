package ac.uk.ebi.biostd.itest.test.security

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.common.config.PersistenceConfig
import ac.uk.ebi.biostd.itest.common.BaseIntegrationTest
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.DefaultUser
import ac.uk.ebi.biostd.itest.entities.RegularUser
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.entities.TestUser
import ac.uk.ebi.biostd.persistence.common.model.AccessType.ATTACH
import ac.uk.ebi.biostd.persistence.model.DbAccessPermission
import ac.uk.ebi.biostd.persistence.repositories.AccessPermissionRepository
import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepo
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.dsl.line
import ebi.ac.uk.dsl.tsv
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(TemporaryFolderExtension::class)
internal class SubmitPermissionTest(tempFolder: TemporaryFolder) : BaseIntegrationTest(tempFolder) {
    @Nested
    @Import(PersistenceConfig::class)
    @ExtendWith(SpringExtension::class)
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @DirtiesContext
    inner class SubmitExtCollectionTest(
        @Autowired private val securityTestService: SecurityTestService,
        @Autowired private val userDataRepository: UserDataRepository,
        @Autowired private val tagsDataRepository: AccessTagDataRepo,
        @Autowired private val accessPermissionRepository: AccessPermissionRepository
    ) {
        @LocalServerPort
        private var serverPort: Int = 0

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
            securityTestService.registerUser(SuperUser)
            securityTestService.registerUser(RegularUser)

            superUserWebClient = getWebClient(serverPort, SuperUser)
            regularUserWebClient = getWebClient(serverPort, RegularUser)
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
                    "The user biostudies-dev@ebi.ac.uk is not allowed to submit to TestProject project")
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
            setAttachPermission(RegularUser, "TestProject2")

            assertThat(regularUserWebClient.submitSingle(submission, SubmissionFormat.TSV)).isSuccessful()
        }

        @Test
        fun `submit to default project`() {
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

            createUser(DefaultUser, serverPort)
            assertThat(superUserWebClient.submitSingle(project, SubmissionFormat.TSV)).isSuccessful()

            setAttachPermission(DefaultUser, "TestProject3")
            assertThat(regularUserWebClient.submitSingle(submission, SubmissionFormat.TSV)).isSuccessful()
        }

        private fun setAttachPermission(testUser: TestUser, project: String) {
            val accessTag = tagsDataRepository.findByName(project)
            val user = userDataRepository.findByEmailAndActive(testUser.email, active = true)
            val attachPermission = DbAccessPermission(accessType = ATTACH, user = user.get(), accessTag = accessTag)
            accessPermissionRepository.save(attachPermission)
        }
    }
}
