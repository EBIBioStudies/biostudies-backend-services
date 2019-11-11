package ac.uk.ebi.biostd.itest.test.submission.query

import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.itest.common.BaseIntegrationTest
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.model.SubmissionDraft
import ebi.ac.uk.util.collections.second
import ebi.ac.uk.util.collections.third
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
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
internal class SubmissionDraftListApiTest(tempFolder: TemporaryFolder) : BaseIntegrationTest(tempFolder) {
    @Nested
    @ExtendWith(SpringExtension::class)
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @Transactional
    @DirtiesContext
    inner class SubmissionDraftListTest {
        @LocalServerPort
        private var serverPort: Int = 0

        private lateinit var webClient: BioWebClient

        @BeforeAll
        fun init() {
            webClient = getWebClient(serverPort, SuperUser)
            createDrafts()
        }

        @Test
        fun `get draft by acc no`() {
            val draft = webClient.getSubmissionDraft("ABC-0")
            assertDraft("ABC-0", draft)
        }

        @Test
        fun `get drafts without pagination`() {
            val drafts = webClient.getAllSubmissionDrafts()

            assertThat(drafts).hasSize(5)
            drafts.forEachIndexed { idx, draft -> assertThat(draft.content.toString().contains("ABC-$idx")).isTrue() }
        }

        @Test
        fun `get drafts with pagination`() {
            val page1 = webClient.getAllSubmissionDrafts(mapOf("offset" to 0, "limit" to 3))
            val page2 = webClient.getAllSubmissionDrafts(mapOf("offset" to 3, "limit" to 3))

            assertThat(page1).hasSize(3)
            assertDraft("ABC-0", page1.first())
            assertDraft("ABC-1", page1.second())
            assertDraft("ABC-2", page1.third())

            assertThat(page2).hasSize(2)
            assertDraft("ABC-3", page2.first())
            assertDraft("ABC-4", page2.second())
        }

        private fun createDrafts() {
            for (idx in 0..4) {
                val pageTab = jsonObj {
                    "accno" to "ABC-$idx"
                    "type" to "Study"
                }.toString()

                webClient.createSubmissionDraft(pageTab)
            }
        }

        private fun assertDraft(accNo: String, draft: SubmissionDraft) =
            assertThat(draft.content.toString().contains(accNo)).isTrue()
    }
}
