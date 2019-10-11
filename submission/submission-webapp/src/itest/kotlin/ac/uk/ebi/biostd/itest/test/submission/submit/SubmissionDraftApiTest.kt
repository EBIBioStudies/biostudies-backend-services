package ac.uk.ebi.biostd.itest.test.submission.submit

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.itest.common.BaseIntegrationTest
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ebi.ac.uk.dsl.json.jsonObj
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
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
            val tmpSubmission = JSONObject(webClient.getSubmissionDraft("ABC-123"))
            assertThat(tmpSubmission.getString("accno")).isEqualTo("ABC-123")
        }

        @Test
        fun `create and get submission draft`() {
            val accession = webClient.createSubmissionDraft(pageTab)
            val resultDraft = webClient.getSubmissionDraft(accession)
            assertThat(resultDraft).isEqualTo(pageTab)
        }

        @Test
        fun `create and update submission draft`() {
            val accession = webClient.createSubmissionDraft(pageTab)
            webClient.updateSubmissionDraft(accession, "new draft content")

            val draftResult = webClient.getSubmissionDraft(accession)
            assertThat(draftResult).isEqualTo("new draft content")
        }

        @Test
        fun `search submission draft`() {
            val accession = webClient.createSubmissionDraft(pageTab)
            val submissions = webClient.searchSubmissionDraft(accession)
            assertThat(submissions).hasSize(1)
            assertThat(submissions.first()).isEqualTo(pageTab)

            webClient.deleteSubmissionDraft(accession)
            assertThat(webClient.searchSubmissionDraft(accession)).isEmpty()
        }
    }
}
