package ac.uk.ebi.biostd.itest.test.security

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.common.config.PersistenceConfig
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.RegularUser
import ac.uk.ebi.biostd.itest.entities.SuperUser
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
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension

@Import(PersistenceConfig::class)
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
    fun `submit resubmit and delete submission`() {
        val submission = tsv {
            line("Submission", "SimpleAcc1")
            line("Title", "Simple Submission")
            line()
        }.toString()

        assertThat(superUserWebClient.submitSingle(submission, TSV)).isSuccessful()
        assertThat(superUserWebClient.submitSingle(submission, TSV)).isSuccessful()
        superUserWebClient.deleteSubmission("SimpleAcc1")
        assertDeletedSubmission("SimpleAcc1", -1)
        assertDeletedSubmission("SimpleAcc1", -2)
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
            line("AttachTo", "ACollection")
            line()
        }.toString()

        assertThat(superUserWebClient.submitSingle(submission, TSV)).isSuccessful()
        superUserWebClient.givePermissionToUser(RegularUser.email, "ACollection", DELETE.name)

        regularUserWebClient.deleteSubmission("SimpleAcc3")
        assertDeletedSubmission("SimpleAcc3")
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

    @Test
    fun `delete with collection admin user`() {
        val submission = tsv {
            line("Submission", "SimpleAcc5")
            line("Title", "Simple Submission")
            line("AttachTo", "ACollection")
            line()
        }.toString()

        assertThat(superUserWebClient.submitSingle(submission, TSV)).isSuccessful()
        superUserWebClient.givePermissionToUser(ExistingUser.email, "ACollection", ADMIN.name)

        existingUserWebClient.deleteSubmission("SimpleAcc5")
        assertDeletedSubmission("SimpleAcc5")
    }

    @Test
    fun `delete subsmissions`() {
        val submission1 = tsv {
            line("Submission", "S-TEST1")
            line("Title", "Test Section Table")
        }.toString()
        val submission2 = tsv {
            line("Submission", "S-TEST2")
            line("Title", "Test Section Table")
        }.toString()
        val submission3 = tsv {
            line("Submission", "S-TEST3")
            line("Title", "Test Section Table")
        }.toString()

        assertThat(superUserWebClient.submitSingle(submission1, TSV)).isSuccessful()
        assertThat(superUserWebClient.submitSingle(submission2, TSV)).isSuccessful()
        assertThat(superUserWebClient.submitSingle(submission3, TSV)).isSuccessful()

        superUserWebClient.deleteSubmissions(listOf("S-TEST1", "S-TEST3"))
        Thread.sleep(5000)

        assertDeletedSubmission("S-TEST1")
        assertDeletedSubmission("S-TEST3")
        assertThat(submissionRepository.getExtByAccNo("S-TEST2")).isNotNull
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
        val collectionFile = tempFolder.createFile("a-collection.tsv", project)

        assertThat(superUserWebClient.submitSingle(collectionFile, emptyList())).isSuccessful()
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
