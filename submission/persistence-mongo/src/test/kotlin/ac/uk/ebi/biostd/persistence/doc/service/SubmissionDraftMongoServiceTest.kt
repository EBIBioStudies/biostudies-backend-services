package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat.JsonFormat.JsonPretty
import ac.uk.ebi.biostd.persistence.common.request.PaginationFilter
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDraftDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft.DraftStatus.ACTIVE
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft.DraftStatus.PROCESSING
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.fullExtSubmission
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
import ebi.ac.uk.extended.model.ExtSection
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockKExtension::class)
internal class SubmissionDraftMongoServiceTest(
    @MockK private val draftDocDataRepository: SubmissionDraftDocDataRepository,
    @MockK private val queryService: SubmissionPersistenceQueryService,
    @MockK private val serializationService: SerializationService,
    @MockK private val toSubmissionMapper: ToSubmissionMapper,
) {
    private val testInstance = SubmissionDraftMongoService(
        draftDocDataRepository,
        queryService,
        serializationService,
        toSubmissionMapper
    )
    private val testDocDraft = DocSubmissionDraft(USER_ID, DRAFT_KEY, DRAFT_CONTENT, ACTIVE)

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun `get draft when exists`() {
        every { draftDocDataRepository.findByUserIdAndKey(USER_ID, DRAFT_KEY) } returns testDocDraft

        val result = testInstance.getSubmissionDraft(USER_ID, DRAFT_KEY)

        assertThat(result.key).isEqualTo(DRAFT_KEY)
        assertThat(result.content).isEqualTo(DRAFT_CONTENT)
        verify(exactly = 0) { draftDocDataRepository.saveDraft(USER_ID, DRAFT_KEY, DRAFT_CONTENT) }
    }

    @Test
    fun `get draft when doesn't exist`() {
        val extSubmission = fullExtSubmission.copy(section = ExtSection(type = "Study"))
        every { queryService.getExtByAccNo(DRAFT_KEY) } returns extSubmission
        every { draftDocDataRepository.findByUserIdAndKey(USER_ID, DRAFT_KEY) } returns null
        every { draftDocDataRepository.createDraft(USER_ID, DRAFT_KEY, DRAFT_CONTENT) } returns testDocDraft
        every {
            serializationService.serializeSubmission(toSubmissionMapper.toSimpleSubmission(extSubmission), JsonPretty)
        } returns DRAFT_CONTENT

        val result = testInstance.getSubmissionDraft(USER_ID, DRAFT_KEY)

        assertThat(result.key).isEqualTo(DRAFT_KEY)
        assertThat(result.content).isEqualTo(DRAFT_CONTENT)
        verify(exactly = 1) { draftDocDataRepository.createDraft(USER_ID, DRAFT_KEY, DRAFT_CONTENT) }
    }

    @Test
    fun `update submission draft`() {
        every { draftDocDataRepository.updateDraftContent(USER_ID, DRAFT_KEY, DRAFT_CONTENT) } answers { nothing }

        val result = testInstance.updateSubmissionDraft(USER_ID, DRAFT_KEY, DRAFT_CONTENT)

        assertThat(result.key).isEqualTo(DRAFT_KEY)
        assertThat(result.content).isEqualTo(DRAFT_CONTENT)
        verify(exactly = 1) { draftDocDataRepository.updateDraftContent(USER_ID, DRAFT_KEY, DRAFT_CONTENT) }
    }

    @Test
    fun deleteSubmissionDraft() {
        every { draftDocDataRepository.deleteByUserIdAndKey(USER_ID, DRAFT_KEY) } returns Unit

        testInstance.deleteSubmissionDraft(USER_ID, DRAFT_KEY)

        verify(exactly = 1) { draftDocDataRepository.deleteByUserIdAndKey(USER_ID, DRAFT_KEY) }
    }

    @Test
    fun `get draft list`() {
        val someFilter = PaginationFilter()
        every { draftDocDataRepository.findAllByUserIdAndStatus(USER_ID, ACTIVE, someFilter) } returns listOf(
            testDocDraft
        )

        val result = testInstance.getActiveSubmissionsDraft(USER_ID, someFilter)

        assertThat(result).hasSize(1)
        assertThat(result[0].key).isEqualTo(DRAFT_KEY)
        assertThat(result[0].content).isEqualTo(DRAFT_CONTENT)
    }

    @Test
    fun `create draft`() {
        mockkStatic(Instant::class)
        val draftCreationTime = 2L
        every { Instant.now().toEpochMilli() } returns draftCreationTime
        every {
            draftDocDataRepository.createDraft(
                USER_ID,
                "TMP_$draftCreationTime",
                DRAFT_CONTENT
            )
        } returns testDocDraft

        val result = testInstance.createSubmissionDraft(USER_ID, DRAFT_CONTENT)

        assertThat(result.key).isEqualTo(DRAFT_KEY)
        assertThat(result.content).isEqualTo(DRAFT_CONTENT)
        unmockkStatic(Instant::class)
    }

    @Test
    fun setActiveStatus() {
        every { draftDocDataRepository.setStatus(USER_ID, DRAFT_KEY, ACTIVE) } answers { nothing }

        testInstance.setActiveStatus(USER_ID, DRAFT_KEY)

        verify(exactly = 1) { draftDocDataRepository.setStatus(USER_ID, DRAFT_KEY, ACTIVE) }
    }

    @Test
    fun setProcessingStatus() {
        every { draftDocDataRepository.setStatus(USER_ID, DRAFT_KEY, PROCESSING) } answers { nothing }

        testInstance.setProcessingStatus(USER_ID, DRAFT_KEY)

        verify(exactly = 1) { draftDocDataRepository.setStatus(USER_ID, DRAFT_KEY, PROCESSING) }
    }

    companion object {
        const val USER_ID = "jhon.doe@ebi.ac.uk"
        const val DRAFT_KEY = "key"
        const val DRAFT_CONTENT = "content"
    }
}
