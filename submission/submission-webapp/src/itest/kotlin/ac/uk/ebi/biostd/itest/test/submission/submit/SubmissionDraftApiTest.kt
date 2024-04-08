package ac.uk.ebi.biostd.itest.test.submission.submit

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.JSON
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.common.TestUserDataService
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.itest.getWebClient
import com.fasterxml.jackson.databind.ObjectMapper
import ebi.ac.uk.dsl.json.jsonObj
import kotlinx.coroutines.runBlocking
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
import uk.ac.ebi.serialization.extensions.getProperty

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Transactional
class SubmissionDraftApiTest(
    @Autowired val securityTestService: SecurityTestService,
    @Autowired val dataService: TestUserDataService,
    @LocalServerPort val serverPort: Int,
) {
    private lateinit var webClient: BioWebClient

    @BeforeAll
    fun init() =
        runBlocking {
            securityTestService.ensureUserRegistration(SuperUser)
            webClient = getWebClient(serverPort, SuperUser)
        }

    @Test
    fun `12-1 get draft submission when draft does not exist but submission does`() {
        val pageTab =
            jsonObj {
                "accno" to "ABC-123"
                "type" to "Study"
            }.toString()

        webClient.submitSingle(pageTab, JSON)

        val draftSubmission = webClient.getSubmissionDraft("ABC-123")
        assertThat(draftSubmission.key).isEqualTo("ABC-123")
        webClient.deleteSubmissionDraft(draftSubmission.key)
    }

    @Test
    fun `12-2 create and get submission draft`() {
        val pageTab =
            jsonObj {
                "accno" to "ABC-124"
                "type" to "Study"
            }.toString()

        val draftSubmission = webClient.createSubmissionDraft(pageTab)

        val resultDraft = webClient.getSubmissionDraft(draftSubmission.key)
        assertEquals(resultDraft.content.toString(), pageTab, false)
        webClient.deleteSubmissionDraft(draftSubmission.key)
    }

    @Test
    fun `12-3 create and update submission draft`() {
        val updatedValue = "{ \"value\": 1 }"
        val pageTab =
            jsonObj {
                "accno" to "ABC-125"
                "type" to "Study"
            }.toString()

        val draftSubmission = webClient.createSubmissionDraft(pageTab)
        webClient.updateSubmissionDraft(draftSubmission.key, "{ \"value\": 1 }")

        val draftResult = webClient.getSubmissionDraft(draftSubmission.key)
        assertEquals(draftResult.content.toString(), updatedValue, false)
        webClient.deleteSubmissionDraft(draftSubmission.key)
    }

    @Test
    fun `12-4 delete submission draft after submission`() {
        val pageTab =
            jsonObj {
                "accno" to "ABC-126"
                "title" to "From Draft"
            }.toString()
        val draft = webClient.createSubmissionDraft(pageTab)

        webClient.submitSingleFromDraft(draft.key)

        assertThat(webClient.getAllSubmissionDrafts()).isEmpty()
    }

    @Test
    fun `12-5 get draft submission when neither draft nor submission exists`() {
        assertThatExceptionOfType(WebClientException::class.java).isThrownBy {
            webClient.getSubmissionDraft("ABC-127")
        }
    }

    @Test
    fun `12-6 delete a draft directly`() {
        val pageTab =
            jsonObj {
                "accno" to "ABC-128"
                "type" to "Study"
            }.toString()
        webClient.submitSingle(pageTab, JSON)

        webClient.deleteSubmissionDraft("ABC-128")

        assertThat(dataService.getUserData(SuperUser.email, "ABC-128")).isNull()
    }

    @Test
    fun `12-7 re submit from draft`() {
        webClient.submitSingle(
            jsonObj {
                "accno" to "ABC-129"
                "type" to "Study"
            }.toString(),
            JSON,
        )

        webClient.updateSubmissionDraft(
            "ABC-129",
            jsonObj {
                "accno" to "ABC-129"
                "ReleaseDate" to "2021-09-21"
                "type" to "Study"
            }.toString(),
        )

        webClient.submitSingleFromDraft("ABC-129")

        assertThat(dataService.getUserData(SuperUser.email, "ABC-129")).isNull()
        assertThat(dataService.getUserData(SuperUser.email, "ABC-129")).isNull()
    }

    @Test
    fun `12-8 update a submission already submitted draft`(
        @Autowired mapper: ObjectMapper,
    ) {
        val accNo = "ABC-130"
        val newSubmission =
            webClient.submitSingle(
                jsonObj {
                    "accno" to accNo
                    "section" to
                        jsonObj {
                            "type" to "Study"
                        }
                    "type" to "submission"
                }.toString(),
            ).body
        assertThat(newSubmission.section.type).isEqualTo("Study")

        webClient.getSubmissionDraft(accNo)
        webClient.updateSubmissionDraft(
            accNo,
            jsonObj {
                "accno" to accNo
                "section" to
                    jsonObj {
                        "type" to "Another"
                    }
                "type" to "submission"
            }.toString(),
        )
        val updatedSubmission = webClient.submitSingleFromDraft(accNo).body
        assertThat(updatedSubmission.section.type).isEqualTo("Another")

        webClient.getSubmissionDraft(accNo)
        webClient.updateSubmissionDraft(
            accNo,
            jsonObj {
                "accno" to accNo
                "section" to
                    jsonObj {
                        "type" to "Yet-Another"
                    }
                "type" to "submission"
            }.toString(),
        )
        assertThat(webClient.getSubmissionDraft(accNo).content.getProperty("section.type")).isEqualTo("Yet-Another")
        webClient.deleteSubmissionDraft(accNo)
    }
}
