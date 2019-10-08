package ac.uk.ebi.biostd.itest

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.common.config.PersistenceConfig
import ac.uk.ebi.biostd.common.config.SubmitterConfig
import ac.uk.ebi.biostd.files.FileConfig
import ac.uk.ebi.biostd.itest.common.TestConfig
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ebi.ac.uk.dsl.line
import ebi.ac.uk.dsl.tsv
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
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

        @BeforeAll
        fun init() {
            val securityClient = SecurityWebClient.create("http://localhost:$serverPort")
            securityClient.registerUser(SuperUser.asRegisterRequest())
            webClient = securityClient.getAuthenticatedClient(SuperUser.email, SuperUser.password)
        }

        @Test
        fun `get draft submission when draft does not exit`() {
            val pageTab = tsv {
                line("Submission", "ABC-123")
                line("Title", "Test Public Submission")
                line()
            }.toString()

            webClient.submitSingle(pageTab, SubmissionFormat.TSV)
            val tmpSubmission = webClient.getSubmissionDraft("ABC-123")
            assertThat(tmpSubmission.key).isEqualTo("ABC-123")
        }

        @Test
        fun `create and get submission draft`() {
            val draft = webClient.createSubmissionDraft("draft content")

            val resultDraft = webClient.getSubmissionDraft(draft.key)
            assertThat(resultDraft.data).isEqualTo(draft.data)
        }

        @Test
        fun `create and update submission draft`() {
            val draft = webClient.createSubmissionDraft("draft content")
            webClient.updateSubmissionDraft(draft.key, "new draft content")

            val draftResult = webClient.getSubmissionDraft(draft.key)
            assertThat(draftResult.data).isEqualTo("new draft content")
        }

        @Test
        fun `search submission draft`() {
            val draft = webClient.createSubmissionDraft("draft content")

            val submissions = webClient.searchSubmissionDraft(draft.key)
            assertThat(submissions).hasSize(1)
            assertThat(submissions.first()).isEqualTo(draft)

            webClient.deleteSubmissionDraft(draft.key)
            assertThat(webClient.searchSubmissionDraft(draft.key)).isEmpty()
        }
    }
}
