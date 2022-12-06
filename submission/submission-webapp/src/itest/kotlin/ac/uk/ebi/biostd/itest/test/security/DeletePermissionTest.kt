package ac.uk.ebi.biostd.itest.test.security

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SubmissionFilesConfig
import ac.uk.ebi.biostd.common.config.FilePersistenceConfig
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
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.io.ext.createFile
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
    fun init() {
        setUpTestUsers()
        setUpTestCollection()
    }

    @Test
    fun `1,1 - submit resubmit and delete submission`() {
        val submission = tsv {
            line("Submission", "DeleteAcc1")
            line("Title", "Simple Submission")
            line()
        }.toString()

        assertThat(superUserWebClient.submitSingle(submission, TSV)).isSuccessful()
        assertThat(superUserWebClient.submitSingle(submission, TSV)).isSuccessful()
        superUserWebClient.deleteSubmission("DeleteAcc1")
        assertDeletedSubmission("DeleteAcc1", -1)
        assertDeletedSubmission("DeleteAcc1", -2)
    }

    @Test
    fun `1,2 - delete with regular user`() {
        val submission = tsv {
            line("Submission", "DeleteAcc2")
            line("Title", "Simple Submission")
            line()
        }.toString()

        assertThat(superUserWebClient.submitSingle(submission, TSV)).isSuccessful()

        assertThatExceptionOfType(WebClientException::class.java).isThrownBy {
            regularUserWebClient.deleteSubmission("DeleteAcc2")
        }
    }

    @Test
    fun `1,3 - delete with regular user and tag access permission`() {
        val submission = tsv {
            line("Submission", "DeleteAcc3")
            line("Title", "Simple Submission")
            line("AttachTo", "ACollection")
            line()
        }.toString()

        assertThat(superUserWebClient.submitSingle(submission, TSV)).isSuccessful()
        superUserWebClient.givePermissionToUser(RegularUser.email, "ACollection", DELETE.name)

        regularUserWebClient.deleteSubmission("DeleteAcc3")
        assertDeletedSubmission("DeleteAcc3")
    }

    @Test
    fun `1,4 - resubmit deleted submission`() {
        val submission = tsv {
            line("Submission", "DeleteAcc4")
            line("Title", "Simple Submission")
            line()
        }.toString()

        superUserWebClient.submitSingle(submission, TSV)
        superUserWebClient.deleteSubmission("DeleteAcc4")
        superUserWebClient.submitSingle(submission, TSV)

        val resubmitted = submissionRepository.getExtByAccNo("DeleteAcc4")
        assertThat(resubmitted.version).isEqualTo(2)
    }

    @Test
    fun `1,5 - delete with collection admin user`() {
        val submission = tsv {
            line("Submission", "DeleteAcc5")
            line("Title", "Simple Submission")
            line("AttachTo", "ACollection")
            line()
        }.toString()

        assertThat(superUserWebClient.submitSingle(submission, TSV)).isSuccessful()
        superUserWebClient.givePermissionToUser(ExistingUser.email, "ACollection", ADMIN.name)

        existingUserWebClient.deleteSubmission("DeleteAcc5")
        assertDeletedSubmission("DeleteAcc5")
    }

    @Test
    fun `1,6 - delete subsmissions`() {
        val submission1 = tsv {
            line("Submission", "DeleteAcc6-1")
            line("Title", "Test Section Table")
        }.toString()
        val submission2 = tsv {
            line("Submission", "DeleteAcc6-2")
            line("Title", "Test Section Table")
        }.toString()
        val submission3 = tsv {
            line("Submission", "DeleteAcc6-3")
            line("Title", "Test Section Table")
        }.toString()

        assertThat(superUserWebClient.submitSingle(submission1, TSV)).isSuccessful()
        assertThat(superUserWebClient.submitSingle(submission2, TSV)).isSuccessful()
        assertThat(superUserWebClient.submitSingle(submission3, TSV)).isSuccessful()

        superUserWebClient.deleteSubmissions(listOf("DeleteAcc6-1", "DeleteAcc6-3"))
        Thread.sleep(5000)

        assertDeletedSubmission("DeleteAcc6-1")
        assertDeletedSubmission("DeleteAcc6-3")
        assertThat(submissionRepository.getExtByAccNo("DeleteAcc6-2")).isNotNull
    }

    private fun assertDeletedSubmission(accNo: String, version: Int = -1) {
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

    private fun setUpTestUsers() {
        securityTestService.ensureUserRegistration(SuperUser)
        securityTestService.ensureUserRegistration(RegularUser)
        securityTestService.ensureUserRegistration(ExistingUser)

        superUserWebClient = getWebClient(serverPort, SuperUser)
        regularUserWebClient = getWebClient(serverPort, RegularUser)
        existingUserWebClient = getWebClient(serverPort, ExistingUser)
    }
}
