package ac.uk.ebi.biostd.itest.test.submission.submit

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.data.service.UserDataService
import ac.uk.ebi.biostd.itest.common.BaseIntegrationTest
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.dsl.line
import ebi.ac.uk.dsl.tsv
import ebi.ac.uk.security.integration.model.api.SecurityUser
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.skyscreamer.jsonassert.JSONAssert.assertEquals
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Transactional

@ExtendWith(TemporaryFolderExtension::class)
internal class SubmissionDraftApiTest(tempFolder: TemporaryFolder) : BaseIntegrationTest(tempFolder) {
    @Nested
    @ExtendWith(SpringExtension::class)
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @Transactional
    @DirtiesContext
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
            securityUser = securityTestService.registerUser(SuperUser)
            webClient = getWebClient(serverPort, SuperUser)
        }

        @Test
        fun `get draft submission when draft does not exist but submission does`() {
            val pageTab = jsonObj { "accno" to "ABC-123"; "type" to "Study" }.toString()

            webClient.submitSingle(pageTab, SubmissionFormat.JSON)

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

            webClient.submitSingle(pageTab, SubmissionFormat.JSON)
            webClient.getSubmissionDraft("ABC-126")
            val updatedDraft = tsv {
                line("Submission", "ABC-126")
                line("Description", "Updated submission")
            }
            webClient.submitSingle(updatedDraft.toString(), SubmissionFormat.TSV)
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
            webClient.submitSingle(pageTab, SubmissionFormat.JSON)

            webClient.deleteSubmissionDraft("ABC-128")

            assertThat(dataService.getUserData(securityUser.id, "ABC-128")).isNull()
        }
    }
}
