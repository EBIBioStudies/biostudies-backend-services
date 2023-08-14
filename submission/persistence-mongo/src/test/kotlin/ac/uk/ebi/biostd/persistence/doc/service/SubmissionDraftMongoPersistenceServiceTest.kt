package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.request.PaginationFilter
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDraftDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft.DraftStatus.ACTIVE
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft.DraftStatus.PROCESSING
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertNotNull

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockKExtension::class)
internal class SubmissionDraftMongoPersistenceServiceTest(
    @MockK private val draftDocDataRepository: SubmissionDraftDocDataRepository,
) {
    private val testInstance = SubmissionDraftMongoPersistenceService(draftDocDataRepository)
    private val testDocDraft = DocSubmissionDraft(USER_ID, DRAFT_KEY, DRAFT_CONTENT, ACTIVE)

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun `get draft when exists`() = runTest {
        coEvery {
            draftDocDataRepository.findByUserIdAndKeyAndStatusIsNot(
                USER_ID,
                DRAFT_KEY,
                DocSubmissionDraft.DraftStatus.ACCEPTED
            )
        } returns testDocDraft

        val result = testInstance.findSubmissionDraft(USER_ID, DRAFT_KEY)

        assertNotNull(result)
        assertThat(result.key).isEqualTo(DRAFT_KEY)
        assertThat(result.content).isEqualTo(DRAFT_CONTENT)
    }

    @Test
    fun `get draft when doesn't exist`() = runTest {
        coEvery {
            draftDocDataRepository.findByUserIdAndKeyAndStatusIsNot(
                USER_ID,
                DRAFT_KEY,
                DocSubmissionDraft.DraftStatus.ACCEPTED
            )
        } returns null

        assertThat(testInstance.findSubmissionDraft(USER_ID, DRAFT_KEY)).isNull()
    }

    @Test
    fun `update submission draft`() = runTest {
        coEvery { draftDocDataRepository.updateDraftContent(USER_ID, DRAFT_KEY, DRAFT_CONTENT) } answers { nothing }

        val result = testInstance.updateSubmissionDraft(USER_ID, DRAFT_KEY, DRAFT_CONTENT)

        assertThat(result.key).isEqualTo(DRAFT_KEY)
        assertThat(result.content).isEqualTo(DRAFT_CONTENT)
        coVerify(exactly = 1) { draftDocDataRepository.updateDraftContent(USER_ID, DRAFT_KEY, DRAFT_CONTENT) }
    }

    @Test
    fun `delete submission draft by user and key`() = runTest {
        coEvery { draftDocDataRepository.deleteByUserIdAndKey(USER_ID, DRAFT_KEY) } returns Unit

        testInstance.deleteSubmissionDraft(USER_ID, DRAFT_KEY)

        coVerify(exactly = 1) { draftDocDataRepository.deleteByUserIdAndKey(USER_ID, DRAFT_KEY) }
    }

    @Test
    fun `get draft list`() = runTest {
        val someFilter = PaginationFilter()
        every { draftDocDataRepository.findAllByUserIdAndStatus(USER_ID, ACTIVE, someFilter) } returns flowOf(
            testDocDraft
        )

        val result = testInstance.getActiveSubmissionDrafts(USER_ID, someFilter).toList()

        assertThat(result).hasSize(1)
        assertThat(result[0].key).isEqualTo(DRAFT_KEY)
        assertThat(result[0].content).isEqualTo(DRAFT_CONTENT)
    }

    @Test
    fun `create draft`() = runTest {
        coEvery {
            draftDocDataRepository.createDraft(
                USER_ID,
                DRAFT_KEY,
                DRAFT_CONTENT
            )
        } returns testDocDraft

        val result = testInstance.createSubmissionDraft(USER_ID, DRAFT_KEY, DRAFT_CONTENT)

        assertThat(result.key).isEqualTo(DRAFT_KEY)
        assertThat(result.content).isEqualTo(DRAFT_CONTENT)
        coVerify(exactly = 1) { draftDocDataRepository.createDraft(USER_ID, DRAFT_KEY, DRAFT_CONTENT) }
    }

    @Test
    fun setActiveStatus() = runTest {
        coEvery { draftDocDataRepository.setStatus(DRAFT_KEY, ACTIVE) } answers { nothing }

        testInstance.setActiveStatus(DRAFT_KEY)

        coVerify(exactly = 1) { draftDocDataRepository.setStatus(DRAFT_KEY, ACTIVE) }
    }

    @Test
    fun setProcessingStatus() = runTest {
        coEvery { draftDocDataRepository.setStatus(USER_ID, DRAFT_KEY, PROCESSING) } answers { nothing }

        testInstance.setProcessingStatus(USER_ID, DRAFT_KEY)

        coVerify(exactly = 1) { draftDocDataRepository.setStatus(USER_ID, DRAFT_KEY, PROCESSING) }
    }

    companion object {
        const val USER_ID = "jhon.doe@ebi.ac.uk"
        const val DRAFT_KEY = "key"
        const val DRAFT_CONTENT = "content"
    }
}
