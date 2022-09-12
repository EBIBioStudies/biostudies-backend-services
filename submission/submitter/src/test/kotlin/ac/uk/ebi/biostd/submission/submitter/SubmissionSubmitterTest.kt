package ac.uk.ebi.biostd.submission.submitter

import ac.uk.ebi.biostd.persistence.common.request.ExtSubmitRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionDraftService
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
    @MockK private val draftService: SubmissionDraftService,
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
        @MockK request: SubmitRequest
    ) {
        val submission = basicExtSubmission
        val extRequestSlot = slot<ExtSubmitRequest>()

        every { request.draftKey } returns "TMP_123"
        every { request.owner } returns submission.owner
        every { request.accNo } returns submission.accNo
        every { submissionProcessor.processSubmission(request) } returns submission
        every { parentInfoService.executeCollectionValidators(submission) } answers { nothing }
        every { draftService.setActiveStatus(submission.owner, "TMP_123") } answers { nothing }
        every { draftService.setProcessingStatus(submission.owner, "TMP_123") } answers { nothing }
        every {
            submissionSubmitter.saveRequest(capture(extRequestSlot))
        } returns (submission.accNo to submission.version)

        testInstance.createRequest(request)

        val extRequest = extRequestSlot.captured
        assertThat(extRequest.draftKey).isEqualTo("TMP_123")
        assertThat(extRequest.submission).isEqualTo(submission)
        verify(exactly = 1) {
            submissionProcessor.processSubmission(request)
            parentInfoService.executeCollectionValidators(submission)
            draftService.setProcessingStatus(submission.owner, "TMP_123")
            submissionSubmitter.saveRequest(extRequest)
        }
        verify(exactly = 0) {
            draftService.setActiveStatus(submission.owner, "TMP_123")
        }
    }

    @Test
    fun `create with failure on validation`(
        @MockK request: SubmitRequest
    ) {
        val submission = basicExtSubmission
        val extRequestSlot = slot<ExtSubmitRequest>()

        every { request.draftKey } returns "TMP_123"
        every { request.owner } returns submission.owner
        every { request.accNo } returns submission.accNo
        every { parentInfoService.executeCollectionValidators(submission) } answers { nothing }
        every { draftService.setActiveStatus(submission.owner, "TMP_123") } answers { nothing }
        every { draftService.setProcessingStatus(submission.owner, "TMP_123") } answers { nothing }
        every { submissionProcessor.processSubmission(request) } throws RuntimeException("validation error")
        every {
            submissionSubmitter.saveRequest(capture(extRequestSlot))
        } returns (submission.accNo to submission.version)

        assertThrows<InvalidSubmissionException> { testInstance.createRequest(request) }

        verify(exactly = 1) {
            submissionProcessor.processSubmission(request)
            draftService.setProcessingStatus(submission.owner, "TMP_123")
            draftService.setActiveStatus(submission.owner, "TMP_123")
        }
        verify(exactly = 0) {
            parentInfoService.executeCollectionValidators(submission)
            submissionSubmitter.saveRequest(capture(extRequestSlot))
        }
    }
}
