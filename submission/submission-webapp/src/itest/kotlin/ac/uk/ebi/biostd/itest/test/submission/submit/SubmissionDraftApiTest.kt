package ac.uk.ebi.biostd.itest.test.submission.submit

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.itest.common.BaseIntegrationTest
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.dsl.line
import ebi.ac.uk.dsl.tsv
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.skyscreamer.jsonassert.JSONAssert.assertEquals
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
    inner class SubmissionDraftTest {
        @LocalServerPort
        private var serverPort: Int = 0

        private lateinit var webClient: BioWebClient

        val pageTab = jsonObj {
            "accno" to "ABC-123"
            "type" to "Study"
        }.toString()

        @BeforeAll
        fun init() {
            webClient = getWebClient(serverPort, SuperUser)
        }

        @Test
        fun `get draft submission when draft does not exit but submissions does`() {
            webClient.submitSingle(pageTab, SubmissionFormat.JSON)
            val draftSubmission = webClient.getSubmissionDraft("ABC-123")
            assertThat(draftSubmission.key).isEqualTo("ABC-123")
        }

        @Test
        fun `create and get submission draft`() {
            val draftSubmission = webClient.createSubmissionDraft(pageTab)
            val resultDraft = webClient.getSubmissionDraft(draftSubmission.key)
            assertEquals(resultDraft.content.toString(), pageTab, false)
        }

        @Test
        fun `create and update submission draft`() {
            val updatedValue = "{ \"value\": 1 }"
            val draftSubmission = webClient.createSubmissionDraft(pageTab)
            webClient.updateSubmissionDraft(draftSubmission.key, "{ \"value\": 1 }")

            val draftResult = webClient.getSubmissionDraft(draftSubmission.key)
            assertEquals(draftResult.content.toString(), updatedValue, false)
        }

        @Test
        fun `search submission draft`() {
            val draftSubmission = webClient.createSubmissionDraft(pageTab)
            val submissions = webClient.searchSubmissionDraft(draftSubmission.key)
            assertThat(submissions).hasSize(1)
            assertEquals(submissions.first().content.toString(), pageTab, true)

            webClient.deleteSubmissionDraft(draftSubmission.key)
            assertThat(webClient.getAllSubmissionDrafts()).isEmpty()
        }

        @Test
        fun `delete submission draft after submission`() {
            webClient.submitSingle(pageTab, SubmissionFormat.JSON)
            webClient.getSubmissionDraft("ABC-123")
            val updatedDraft = tsv {
                line("Submission", "ABC-123")
                line("Description", "Updated submission")
            }
            webClient.submitSingle(updatedDraft.toString(), SubmissionFormat.TSV)
            assertThat(webClient.getAllSubmissionDrafts()).isEmpty()
        }
    }
}
