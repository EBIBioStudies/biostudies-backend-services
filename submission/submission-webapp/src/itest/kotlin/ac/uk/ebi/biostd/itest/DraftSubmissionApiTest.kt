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
import ebi.ac.uk.model.DraftSubmission
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
class DraftSubmissionApiTest {
    @Nested
    @ExtendWith(SpringExtension::class)
    @Import(value = [TestConfig::class, SubmitterConfig::class, PersistenceConfig::class, FileConfig::class])
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @Transactional
    @DirtiesContext
    inner class DraftSubmissionTest {
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
        fun getDraftSubmission() {
            val pageTab = tsv {
                line("Submission", "ABC-123")
                line("Title", "Test Public Submission")
                line()
            }.toString()

            webClient.submitSingle(pageTab, SubmissionFormat.TSV)
            val tmpSubmission = webClient.getDraftSubmission("ABC-123")
            assertThat(tmpSubmission.key).isEqualTo("ABC-123")
        }

        @Test
        fun getDraftSubmissions() {
            webClient.saveDraftSubmission("XYZ", "content")

            val submissions = webClient.searchDraftSubmission("XY")

            assertThat(submissions).hasSize(1)
            assertThat(submissions.first()).isEqualTo(DraftSubmission("XYZ", "content"))

            webClient.deleteDraftSubmission("XYZ")
            assertThat(webClient.searchDraftSubmission("XY")).isEmpty()
        }
    }
}
