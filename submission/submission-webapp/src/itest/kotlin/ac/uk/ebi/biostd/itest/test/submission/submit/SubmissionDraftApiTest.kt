package ac.uk.ebi.biostd.itest.test.submission.submit

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.JSON
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.data.service.UserDataService
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ebi.ac.uk.dsl.json.jsonObj
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.skyscreamer.jsonassert.JSONAssert.assertEquals
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Transactional

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Transactional
class SubmissionDraftApiTest(
    @Autowired val securityTestService: SecurityTestService,
    @Autowired val dataService: UserDataService,
    @LocalServerPort val serverPort: Int,
) {
    private lateinit var webClient: BioWebClient

    @BeforeAll
    fun init() {
        securityTestService.ensureUserRegistration(SuperUser)
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
        val pageTab = jsonObj { "accno" to "ABC-126"; "title" to "From Draft" }.toString()
        val draft = webClient.createSubmissionDraft(pageTab)

        webClient.submitSingleFromDraft(draft.key)

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

        assertThat(dataService.getUserData(SuperUser.email, "ABC-128")).isNull()
    }

    @Test
    fun `re submit from draft`() {
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
        assertThat(dataService.getUserData(SuperUser.email, "ABC-129")).isNull()
        assertThat(dataService.getUserData(SuperUser.email, draftResponse.key)).isNull()
    }
}
