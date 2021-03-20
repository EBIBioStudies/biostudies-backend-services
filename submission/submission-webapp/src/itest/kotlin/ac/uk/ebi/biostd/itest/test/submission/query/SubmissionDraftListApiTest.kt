package ac.uk.ebi.biostd.itest.test.submission.query

import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.itest.common.BaseIntegrationTest
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.model.WebSubmissionDraft
import ebi.ac.uk.util.collections.second
import ebi.ac.uk.util.collections.third
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
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
    inner class SubmissionDraftListTest(
        @Autowired val securityTestService: SecurityTestService
    ) {
        @LocalServerPort
        private var serverPort: Int = 0

        private lateinit var webClient: BioWebClient
        private lateinit var testDrafts: List<WebSubmissionDraft>

        @BeforeAll
        fun init() {
            securityTestService.registerUser(SuperUser)
            webClient = getWebClient(serverPort, SuperUser)
            testDrafts = createDrafts()
        }

        @Test
        fun `get draft by key`() {
            val draft = webClient.getSubmissionDraft(testDrafts.first().key)
            assertDraft(testDrafts.first().key, "ABC-0", draft)
        }

        @Test
        fun `get drafts without pagination`() {
            val drafts = webClient.getAllSubmissionDrafts()

            assertThat(drafts).hasSize(3)
            drafts.forEachIndexed { idx, draft -> assertDraft(drafts[idx].key, "ABC-$idx", draft) }
        }

        @Test
        fun `get drafts with pagination`() {
            val page1 = webClient.getAllSubmissionDrafts(offset = 0, limit = 2)
            val page2 = webClient.getAllSubmissionDrafts(offset = 2, limit = 2)

            assertThat(page1).hasSize(2)
            assertDraft(testDrafts.first().key, "ABC-0", page1.first())
            assertDraft(testDrafts.second().key, "ABC-1", page1.second())

            assertThat(page2).hasSize(1)
            assertDraft(testDrafts.third().key, "ABC-2", page2.first())
        }

        private fun createDrafts(): List<WebSubmissionDraft> {
            val drafts = mutableListOf<WebSubmissionDraft>()

            for (idx in 0..2) {
                val pageTab = jsonObj {
                    "accno" to "ABC-$idx"
                    "type" to "Study"
                }.toString()

                drafts.add(webClient.createSubmissionDraft(pageTab))
            }

            return drafts.toList()
        }

        private fun assertDraft(key: String, accNo: String, draft: WebSubmissionDraft) {
            assertThat(draft.key).isEqualTo(key)
            assertThat(draft.content.toString().contains(accNo)).isTrue()
        }
    }
}
