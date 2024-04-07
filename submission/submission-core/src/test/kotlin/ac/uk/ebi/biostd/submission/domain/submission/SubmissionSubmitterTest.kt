package ac.uk.ebi.biostd.submission.domain.submission

import ac.uk.ebi.biostd.persistence.common.request.ExtSubmitRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionDraftPersistenceService
import ac.uk.ebi.biostd.submission.domain.submitter.LocalExtSubmissionSubmitter
import ac.uk.ebi.biostd.submission.exceptions.InvalidSubmissionException
import ac.uk.ebi.biostd.submission.model.SubmitRequest
import ac.uk.ebi.biostd.submission.validator.collection.CollectionValidationService
import ebi.ac.uk.test.basicExtSubmission
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ebi.ac.uk.asserts.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
@OptIn(ExperimentalCoroutinesApi::class)
class SubmissionSubmitterTest(
    @MockK private val request: SubmitRequest,
    @MockK private val submissionSubmitter: LocalExtSubmissionSubmitter,
    @MockK private val submissionProcessor: SubmissionProcessor,
    @MockK private val collectionValidationService: CollectionValidationService,
    @MockK private val draftService: SubmissionDraftPersistenceService,
) {
    private val testInstance = SubmissionSubmitter(
        submissionSubmitter,
        submissionProcessor,
        collectionValidationService,
        draftService,
    )

    @AfterEach
    fun afterEach() = clearAllMocks()

    @BeforeEach
    fun beforeEach() {
        setUpRequest()
        setUpDraftService()
    }

    @Test
    fun `create request`() = runTest {
        val submission = basicExtSubmission
        val extRequestSlot = slot<ExtSubmitRequest>()

        coEvery { submissionProcessor.processSubmission(request) } returns submission
        coEvery { collectionValidationService.executeCollectionValidators(submission) } answers { nothing }
        coEvery {
            submissionSubmitter.createRequest(capture(extRequestSlot))
        } returns (submission.accNo to submission.version)

        testInstance.createRequest(request)

        val extRequest = extRequestSlot.captured
        assertThat(extRequest.draftKey).isEqualTo("TMP_123")
        assertThat(extRequest.submission).isEqualTo(submission)
        coVerify(exactly = 1) {
            submissionProcessor.processSubmission(request)
            collectionValidationService.executeCollectionValidators(submission)
            draftService.setProcessingStatus(submission.owner, "TMP_123")
            submissionSubmitter.createRequest(extRequest)
            draftService.setAcceptedStatus("TMP_123")
        }
        coVerify(exactly = 0) {
            draftService.setActiveStatus("TMP_123")
        }
    }

    @Test
    fun `create with failure on validation`() = runTest {
        val submission = basicExtSubmission
        val extRequestSlot = slot<ExtSubmitRequest>()

        coEvery { submissionProcessor.processSubmission(request) } throws RuntimeException("validation error")

        assertThrows<InvalidSubmissionException> { testInstance.createRequest(request) }

        coVerify(exactly = 1) {
            submissionProcessor.processSubmission(request)
            draftService.setProcessingStatus(submission.owner, "TMP_123")
            draftService.setActiveStatus("TMP_123")
        }
        coVerify(exactly = 0) {
            collectionValidationService.executeCollectionValidators(submission)
            submissionSubmitter.createRequest(capture(extRequestSlot))
            draftService.setAcceptedStatus("TMP_123")
        }
    }

    private fun setUpRequest() {
        every { request.draftKey } returns "TMP_123"
        every { request.owner } returns basicExtSubmission.owner
        every { request.accNo } returns basicExtSubmission.accNo
        every { request.previousVersion } returns null
    }

    private fun setUpDraftService() {
        coEvery { draftService.setAcceptedStatus("TMP_123") } answers { nothing }
        coEvery { draftService.setActiveStatus("TMP_123") } answers { nothing }
        coEvery { draftService.setAcceptedStatus("S-TEST123") } answers { nothing }
        coEvery { draftService.setProcessingStatus(basicExtSubmission.owner, "TMP_123") } answers { nothing }
        coEvery { draftService.deleteSubmissionDraft(basicExtSubmission.submitter, "S-TEST123") } answers { nothing }
    }
}
