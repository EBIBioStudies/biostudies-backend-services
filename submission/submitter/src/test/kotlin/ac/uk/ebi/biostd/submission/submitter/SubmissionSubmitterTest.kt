package ac.uk.ebi.biostd.submission.submitter

import ac.uk.ebi.biostd.persistence.common.request.ExtSubmitRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionDraftPersistenceService
import ac.uk.ebi.biostd.submission.exceptions.InvalidSubmissionException
import ac.uk.ebi.biostd.submission.model.SubmitRequest
import ac.uk.ebi.biostd.submission.service.ParentInfoService
import ebi.ac.uk.test.basicExtSubmission
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class SubmissionSubmitterTest(
    @MockK private val submissionSubmitter: ExtSubmissionSubmitter,
    @MockK private val submissionProcessor: SubmissionProcessor,
    @MockK private val parentInfoService: ParentInfoService,
    @MockK private val draftService: SubmissionDraftPersistenceService,
) {
    private val testInstance = SubmissionSubmitter(
        submissionSubmitter,
        submissionProcessor,
        parentInfoService,
        draftService,
    )

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun `create request`(
        @MockK request: SubmitRequest,
    ) {
        val submission = basicExtSubmission
        val extRequestSlot = slot<ExtSubmitRequest>()

        every { request.draftKey } returns "TMP_123"
        every { request.owner } returns submission.owner
        every { request.accNo } returns submission.accNo
        every { submissionProcessor.processSubmission(request) } returns submission
        every { draftService.setDeleteStatus("TMP_123") } answers { nothing }
        every { parentInfoService.executeCollectionValidators(submission) } answers { nothing }
        every { draftService.setActiveStatus(submission.owner, "TMP_123") } answers { nothing }
        every { draftService.setProcessingStatus(submission.owner, "TMP_123") } answers { nothing }
        every { draftService.deleteSubmissionDraft(submission.owner, "S-TEST123") } answers { nothing }
        every { draftService.deleteSubmissionDraft(submission.submitter, "S-TEST123") } answers { nothing }
        every {
            submissionSubmitter.createRequest(capture(extRequestSlot))
        } returns (submission.accNo to submission.version)

        testInstance.createRequest(request)

        val extRequest = extRequestSlot.captured
        assertThat(extRequest.draftKey).isEqualTo("TMP_123")
        assertThat(extRequest.submission).isEqualTo(submission)
        verify(exactly = 1) {
            submissionProcessor.processSubmission(request)
            parentInfoService.executeCollectionValidators(submission)
            draftService.setProcessingStatus(submission.owner, "TMP_123")
            submissionSubmitter.createRequest(extRequest)
            draftService.setDeleteStatus("TMP_123")
            draftService.deleteSubmissionDraft(submission.owner, "S-TEST123")
            draftService.deleteSubmissionDraft(submission.submitter, "S-TEST123")
        }
        verify(exactly = 0) {
            draftService.setActiveStatus(submission.owner, "TMP_123")
        }
    }

    @Test
    fun `create with failure on validation`(
        @MockK request: SubmitRequest,
    ) {
        val submission = basicExtSubmission
        val extRequestSlot = slot<ExtSubmitRequest>()

        every { request.draftKey } returns "TMP_123"
        every { request.owner } returns submission.owner
        every { request.accNo } returns submission.accNo
        every { draftService.setActiveStatus(submission.owner, "TMP_123") } answers { nothing }
        every { draftService.setProcessingStatus(submission.owner, "TMP_123") } answers { nothing }
        every { submissionProcessor.processSubmission(request) } throws RuntimeException("validation error")

        assertThrows<InvalidSubmissionException> { testInstance.createRequest(request) }

        verify(exactly = 1) {
            submissionProcessor.processSubmission(request)
            draftService.setProcessingStatus(submission.owner, "TMP_123")
            draftService.setActiveStatus(submission.owner, "TMP_123")
        }
        verify(exactly = 0) {
            parentInfoService.executeCollectionValidators(submission)
            submissionSubmitter.createRequest(capture(extRequestSlot))
            draftService.setDeleteStatus("TMP_123")
            draftService.deleteSubmissionDraft(submission.owner, "S-TEST123")
            draftService.deleteSubmissionDraft(submission.submitter, "S-TEST123")
        }
    }
}
