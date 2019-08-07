package ac.uk.ebi.biostd.itest

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.common.config.PersistenceConfig
import ac.uk.ebi.biostd.common.config.SubmitterConfig
import ac.uk.ebi.biostd.files.FileConfig
import ac.uk.ebi.biostd.itest.assertions.SubmissionAssertHelper
import ac.uk.ebi.biostd.itest.common.BaseIntegrationTest
import ac.uk.ebi.biostd.itest.common.TestConfig
import ac.uk.ebi.biostd.itest.entities.GenericUser
import ac.uk.ebi.biostd.itest.factory.allInOneSubmissionJson
import ac.uk.ebi.biostd.itest.factory.allInOneSubmissionTsv
import ac.uk.ebi.biostd.itest.factory.allInOneSubmissionXml
import ac.uk.ebi.biostd.itest.factory.invalidLinkUrl
import ac.uk.ebi.biostd.itest.factory.simpleSubmissionTsv
import ac.uk.ebi.biostd.persistence.model.AccessTag
import ac.uk.ebi.biostd.persistence.repositories.TagsDataRepository
import ac.uk.ebi.biostd.persistence.service.SubmissionRepository
import ebi.ac.uk.api.security.RegisterRequest
import ebi.ac.uk.dsl.line
import ebi.ac.uk.dsl.tsv
import ebi.ac.uk.model.extensions.title
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.client.HttpClientErrorException

/**
 * Integration test for submission in all formats using "all features includes" submission example.
 */
@ExtendWith(TemporaryFolderExtension::class)
internal class SubmissionTest(private val tempFolder: TemporaryFolder) : BaseIntegrationTest(tempFolder) {
    @Nested
    @ExtendWith(SpringExtension::class)
    @Import(value = [TestConfig::class, SubmitterConfig::class, PersistenceConfig::class, FileConfig::class])
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @DirtiesContext
    inner class SingleSubmissionTest(
        @Autowired val tagsDataRepository: TagsDataRepository,
        @Autowired val submissionRepository: SubmissionRepository
    ) {
        @LocalServerPort
        private var serverPort: Int = 0

        private lateinit var webClient: BioWebClient

        private lateinit var assertHelper: SubmissionAssertHelper

        @BeforeAll
        fun init() {
            val securityClient = SecurityWebClient.create("http://localhost:$serverPort")
            securityClient.registerUser(RegisterRequest(GenericUser.email, GenericUser.username, GenericUser.password))
            webClient = securityClient.getAuthenticatedClient(GenericUser.username, GenericUser.password)
            assertHelper = SubmissionAssertHelper(basePath)

            tempFolder.createDirectory("Folder1")
            tempFolder.createDirectory("Folder1/Folder2")

            webClient.uploadFiles(listOf(tempFolder.createFile("DataFile1.txt"), tempFolder.createFile("DataFile2.txt")))
            webClient.uploadFiles(listOf(tempFolder.createFile("Folder1/DataFile3.txt")), "Folder1")
            webClient.uploadFiles(listOf(tempFolder.createFile("Folder1/Folder2/DataFile4.txt")), "Folder1/Folder2")

            tagsDataRepository.save(AccessTag(name = "Public"))
        }

        @Test
        fun `submit all in one TSV submission`() {
            val accNo = "S-EPMC124"

            val response = webClient.submitSingle(allInOneSubmissionTsv(accNo).toString(), SubmissionFormat.TSV)
            assertThat(response).isNotNull
            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
            assertHelper.assertSavedSubmission(accNo, submissionRepository.getExtendedByAccNo(accNo))
        }

        @Test
        fun `submit all in one JSON submission`() {
            val accNo = "S-EPMC125"

            val response = webClient.submitSingle(allInOneSubmissionJson(accNo).toString(), SubmissionFormat.JSON)
            assertThat(response).isNotNull
            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
            assertHelper.assertSavedSubmission(accNo, submissionRepository.getExtendedByAccNo(accNo))
        }

        @Test
        fun `submit all in one XML submission`() {
            val accNo = "S-EPMC126"

            val response = webClient.submitSingle(allInOneSubmissionXml(accNo).toString(), SubmissionFormat.XML)
            assertThat(response).isNotNull
            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
            assertHelper.assertSavedSubmission(accNo, submissionRepository.getExtendedByAccNo(accNo))
        }

        @Test
        fun `resubmit existing submission`() {
            val accNo = "S-ABC123"
            val title = "Simple Submission"
            val submission = simpleSubmissionTsv().toString()
            val response = webClient.submitSingle(submission, SubmissionFormat.TSV)
            assertThat(response).isNotNull
            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
            assertExtSubmission(accNo, title)

            val resubmitResponse = webClient.submitSingle(submission, SubmissionFormat.TSV)
            assertThat(resubmitResponse).isNotNull
            assertThat(resubmitResponse.statusCode).isEqualTo(HttpStatus.OK)
            assertExtSubmission(accNo, title, 2)
        }

        @Test
        fun `new submission with past release date`() {
            val pageTab = tsv {
                line("Submission", "S-RLSD123")
                line("Title", "Test Public Submission")
                line("ReleaseDate", "2000-01-31")
                line()
            }.toString()

            val response = webClient.submitSingle(pageTab, SubmissionFormat.TSV)
            assertThat(response).isNotNull
            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

            val submission = submissionRepository.getExtendedByAccNo("S-RLSD123")
            assertThat(submission.accNo).isEqualTo("S-RLSD123")
            assertThat(submission.title).isEqualTo("Test Public Submission")
            assertThat(submission.released).isTrue()
            assertThat(submission.accessTags).hasSize(1)
            assertThat(submission.accessTags.first()).isEqualTo("Public")
        }

        @Test
        fun `submit with invalid link Url`() {
            val exception = assertThrows(HttpClientErrorException::class.java) {
                webClient.submitSingle(invalidLinkUrl().toString(), SubmissionFormat.TSV)
            }

            assertThat(exception.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        }

        private fun assertExtSubmission(accNo: String, expectedTitle: String, expectedVersion: Int = 1) {
            val submission = submissionRepository.getExtendedByAccNo(accNo)

            assertThat(submission.title).isEqualTo(expectedTitle)
            assertThat(submission.version).isEqualTo(expectedVersion)
        }
    }
}
