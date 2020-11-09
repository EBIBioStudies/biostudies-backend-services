package ac.uk.ebi.biostd.itest.test.security

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.common.config.PersistenceConfig
import ac.uk.ebi.biostd.itest.common.BaseIntegrationTest
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.RegularUser
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.persistence.model.AccessPermission
import ac.uk.ebi.biostd.persistence.model.AccessType.DELETE
import ac.uk.ebi.biostd.persistence.repositories.AccessPermissionRepository
import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepo
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ac.uk.ebi.biostd.persistence.repositories.data.SubmissionRepository
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.dsl.line
import ebi.ac.uk.dsl.tsv
import ebi.ac.uk.test.createFile
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
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
internal class DeletePermissionTest(private val tempFolder: TemporaryFolder) : BaseIntegrationTest(tempFolder) {
    @Nested
    @Import(PersistenceConfig::class)
    @ExtendWith(SpringExtension::class)
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @DirtiesContext
    inner class DeleteSubmissionTest(
        @Autowired private val securityTestService: SecurityTestService,
        @Autowired private val userDataRepository: UserDataRepository,
        @Autowired private val submissionRepository: SubmissionRepository,
        @Autowired private val tagsDataRepository: AccessTagDataRepo,
        @Autowired private val accessPermissionRepository: AccessPermissionRepository
    ) {
        @LocalServerPort
        private var serverPort: Int = 0

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
        fun `submit and delete submission`() {
            val submission = tsv {
                line("Submission", "SimpleAcc1")
                line("Title", "Simple Submission")
                line()
            }.toString()

            assertThat(superUserWebClient.submitSingle(submission, TSV)).isSuccessful()
            superUserWebClient.deleteSubmission("SimpleAcc1")

            val deletedSubmission = submissionRepository.getExtByAccNoAndVersion("SimpleAcc1", -1)
            assertThat(deletedSubmission.version).isEqualTo(-1)
        }

        @Test
        fun `delete with regular user`() {
            val submission = tsv {
                line("Submission", "SimpleAcc2")
                line("Title", "Simple Submission")
                line()
            }.toString()

            assertThat(superUserWebClient.submitSingle(submission, TSV)).isSuccessful()

            assertThatExceptionOfType(WebClientException::class.java).isThrownBy {
                regularUserWebClient.deleteSubmission("SimpleAcc2")
            }
        }

        @Test
        fun `delete with regular user and tag access permission`() {
            val submission = tsv {
                line("Submission", "SimpleAcc3")
                line("Title", "Simple Submission")
                line("AttachTo", "AProject")
                line()
            }.toString()

            setUpPermissions()
            assertThat(superUserWebClient.submitSingle(submission, TSV)).isSuccessful()

            regularUserWebClient.deleteSubmission("SimpleAcc3")

            val deletedSubmission = submissionRepository.getExtByAccNoAndVersion("SimpleAcc3", -1)
            assertThat(deletedSubmission.version).isEqualTo(-1)
        }

        @Test
        fun `resubmit deleted submission`() {
            val submission = tsv {
                line("Submission", "SimpleAcc4")
                line("Title", "Simple Submission")
                line()
            }.toString()

            superUserWebClient.submitSingle(submission, TSV)
            superUserWebClient.deleteSubmission("SimpleAcc4")
            superUserWebClient.submitSingle(submission, TSV)

            val resubmitted = submissionRepository.getExtByAccNo("SimpleAcc4")
            assertThat(resubmitted.version).isEqualTo(2)
        }

        private fun setUpPermissions() {
            val project = tsv {
                line("Submission", "AProject")
                line("AccNoTemplate", "!{S-APR}")
                line()

                line("Project")
            }.toString()
            val projectFile = tempFolder.createFile("a-project.tsv", project)

            assertThat(superUserWebClient.submitSingle(projectFile, emptyList())).isSuccessful()

            val accessTag = tagsDataRepository.findByName("AProject")
            val user = userDataRepository.findByEmailAndActive(RegularUser.email, active = true)
            val accessPermission = AccessPermission(accessType = DELETE, user = user.get(), accessTag = accessTag)
            accessPermissionRepository.save(accessPermission)
        }
    }
}
