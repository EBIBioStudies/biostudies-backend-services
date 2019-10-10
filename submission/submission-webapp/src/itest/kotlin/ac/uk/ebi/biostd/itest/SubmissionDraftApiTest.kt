package ac.uk.ebi.biostd.itest

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.common.config.PersistenceConfig
import ac.uk.ebi.biostd.common.config.SubmitterConfig
import ac.uk.ebi.biostd.files.FileConfig
import ac.uk.ebi.biostd.itest.common.TestConfig
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ebi.ac.uk.dsl.json.jsonObj
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Transactional

@ExtendWith(TemporaryFolderExtension::class)
class SubmissionDraftApiTest {
    @Nested
    @ExtendWith(SpringExtension::class)
    @Import(value = [TestConfig::class, SubmitterConfig::class, PersistenceConfig::class, FileConfig::class])
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
            val securityClient = SecurityWebClient.create("http://localhost:$serverPort")
            securityClient.registerUser(SuperUser.asRegisterRequest())
            webClient = securityClient.getAuthenticatedClient(SuperUser.email, SuperUser.password)
        }

        @Test
        fun `get draft submission when draft does not exit but submissions does`() {
            webClient.submitSingle(pageTab, SubmissionFormat.JSON)
            val tmpSubmission = JSONObject(webClient.getSubmissionDraft("ABC-123"))
            assertThat(tmpSubmission.getString("accno")).isEqualTo("ABC-123")
        }

        @Test
        fun `create and get submission draft`() {
            val submissionDraftKey = webClient.createSubmissionDraft(pageTab)
            val resultDraft = webClient.getSubmissionDraft(submissionDraftKey.accno)
            assertThat(resultDraft).isEqualTo(pageTab)
        }

        @Test
        fun `create and update submission draft`() {
            val submissionDraftKey = webClient.createSubmissionDraft(pageTab)
            webClient.updateSubmissionDraft(submissionDraftKey.accno, "new draft content")

            val draftResult = webClient.getSubmissionDraft(submissionDraftKey.accno)
            assertThat(draftResult).isEqualTo("new draft content")
        }

        @Test
        fun `search submission draft`() {
            val submissionDraftKey = webClient.createSubmissionDraft(pageTab)
            val submissions = webClient.searchSubmissionDraft(submissionDraftKey.accno)
            assertThat(submissions).hasSize(1)
            assertThat(submissions.first()).isEqualTo(pageTab)

            webClient.deleteSubmissionDraft(submissionDraftKey.accno)
            assertThat(webClient.searchSubmissionDraft(submissionDraftKey.accno)).isEmpty()
        }
    }
}
