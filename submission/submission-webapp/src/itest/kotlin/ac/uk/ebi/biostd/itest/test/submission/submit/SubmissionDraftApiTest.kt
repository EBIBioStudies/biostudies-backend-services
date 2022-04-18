package ac.uk.ebi.biostd.itest.test.submission.submit

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.JSON
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.data.service.UserDataService
import ac.uk.ebi.biostd.itest.common.DummyBaseIntegrationTest
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.common.clean
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.listener.ITestListener
import ac.uk.ebi.biostd.itest.listener.ITestListener.Companion.tempFolder
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.security.integration.model.api.SecurityUser
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import java.io.File
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.skyscreamer.jsonassert.JSONAssert.assertEquals
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Transactional

internal class SubmissionDraftApiTest : DummyBaseIntegrationTest() {
    @Nested
    @ExtendWith(SpringExtension::class)
    @SpringBootTest(webEnvironment = RANDOM_PORT)
    @Transactional
    inner class SubmissionDraftTest(
        @Autowired val securityTestService: SecurityTestService,
        @Autowired val dataService: UserDataService
    ) {
        @LocalServerPort
        private var serverPort: Int = 0
        private lateinit var securityUser: SecurityUser
        private lateinit var webClient: BioWebClient

        @BeforeAll
        fun init() {
            tempFolder.clean()
            securityTestService.deleteSuperUser()

            securityUser = securityTestService.registerUser(SuperUser)
            webClient = getWebClient(serverPort, SuperUser)
        }

        @Test
        fun `get draft submission when draft does not exist but submission does`() {
            val pageTab = jsonObj { "accno" to "ABC-123"; "type" to "Study" }.toString()

            webClient.submitSingle(pageTab, JSON)

            val draftSubmission = webClient.getSubmissionDraft("ABC-123")
            assertThat(draftSubmission.key).isEqualTo("ABC-123")
        }

        @Test
        fun `create and get submission draft`() {
            val pageTab = jsonObj { "accno" to "ABC-124"; "type" to "Study" }.toString()

            val draftSubmission = webClient.createSubmissionDraft(pageTab)

            val resultDraft = webClient.getSubmissionDraft(draftSubmission.key)
            assertEquals(resultDraft.content.toString(), pageTab, false)
        }

        @Test
        fun `create and update submission draft`() {
            val updatedValue = "{ \"value\": 1 }"
            val pageTab = jsonObj { "accno" to "ABC-125"; "type" to "Study" }.toString()

            val draftSubmission = webClient.createSubmissionDraft(pageTab)
            webClient.updateSubmissionDraft(draftSubmission.key, "{ \"value\": 1 }")

            val draftResult = webClient.getSubmissionDraft(draftSubmission.key)
            assertEquals(draftResult.content.toString(), updatedValue, false)
        }

        @Test
        fun `delete submission draft after submission`() {
            val pageTab = jsonObj { "accno" to "ABC-126"; "type" to "Study" }.toString()

            webClient.submitSingle(pageTab, JSON)
            webClient.getSubmissionDraft("ABC-126")
            val updatedDraft = tsv {
                line("Submission", "ABC-126")
                line("Description", "Updated Submission")
            }

            webClient.submitSingle(updatedDraft.toString(), TSV)
            assertThat(webClient.getAllSubmissionDrafts()).isEmpty()
        }

        @Test
        fun `get draft submission when neither draft nor submission exists`() {
            assertThatExceptionOfType(WebClientException::class.java).isThrownBy {
                webClient.getSubmissionDraft("ABC-127")
            }
        }

        @Test
        fun `delete a draft directly`() {
            val pageTab = jsonObj { "accno" to "ABC-128"; "type" to "Study" }.toString()
            webClient.submitSingle(pageTab, JSON)

            webClient.deleteSubmissionDraft("ABC-128")

            assertThat(dataService.getUserData(securityUser.email, "ABC-128")).isNull()
        }

        @Test
        fun `submit from draft`() {
            val pageTab = jsonObj {
                "accno" to "ABC-129"
                "type" to "Study"
            }.toString()

            webClient.submitSingle(pageTab, JSON)
            webClient.getSubmissionDraft("ABC-129")

            val updatedDraft = jsonObj {
                "accno" to "ABC-129"
                "ReleaseDate" to "2021-09-21"
                "type" to "Study"
            }.toString()
            val draftResponse = webClient.createSubmissionDraft(updatedDraft)

            webClient.submitSingleFromDraft(draftResponse.key)

            // TODO this approach must be improved once the testing for async submissions are in place
            Thread.sleep(10000)
            assertThat(dataService.getUserData(securityUser.email, "ABC-129")).isNull()
            assertThat(dataService.getUserData(securityUser.email, draftResponse.key)).isNull()
        }
    }
}
