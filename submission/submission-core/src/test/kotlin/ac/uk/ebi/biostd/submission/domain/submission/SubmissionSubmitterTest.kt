package ac.uk.ebi.biostd.submission.domain.submission

import ac.uk.ebi.biostd.persistence.common.request.ExtSubmitRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionDraftPersistenceService
import ac.uk.ebi.biostd.submission.domain.submitter.ExtSubmissionSubmitter
import ac.uk.ebi.biostd.submission.exceptions.InvalidSubmissionException
import ac.uk.ebi.biostd.submission.model.SubmitRequest
import ac.uk.ebi.biostd.submission.validator.collection.CollectionValidationService
import ebi.ac.uk.asserts.assertThrows
import ebi.ac.uk.test.basicExtSubmission
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class SubmissionSubmitterTest(
    @MockK private val request: SubmitRequest,
    @MockK private val submitter: ExtSubmissionSubmitter,
    @MockK private val submissionProcessor: SubmissionProcessor,
    @MockK private val collectionValidationService: CollectionValidationService,
    @MockK private val draftService: SubmissionDraftPersistenceService,
) {
    private val testInstance =
        SubmissionSubmitter(
            submitter,
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
    fun `create request`() =
        runTest {
            val sub = basicExtSubmission
            val extRequestSlot = slot<ExtSubmitRequest>()

            coEvery { submissionProcessor.processSubmission(request) } returns sub
            coEvery { collectionValidationService.executeCollectionValidators(sub) } answers { nothing }
            coEvery { submitter.createRqt(capture(extRequestSlot)) } returns (sub.accNo to sub.version)

            testInstance.createRqt(request)

            val extRequest = extRequestSlot.captured
            assertThat(extRequest.draftKey).isEqualTo("TMP_123")
            assertThat(extRequest.submission).isEqualTo(sub)
            coVerify(exactly = 1) {
                submissionProcessor.processSubmission(request)
                collectionValidationService.executeCollectionValidators(sub)
                draftService.setProcessingStatus(sub.owner, "TMP_123")
                submitter.createRqt(extRequest)
                draftService.setAcceptedStatus("TMP_123")
            }
            coVerify(exactly = 0) {
                draftService.setActiveStatus("TMP_123")
            }
        }

    @Test
    fun `create with failure on validation`() =
        runTest {
            val submission = basicExtSubmission
            val extRequestSlot = slot<ExtSubmitRequest>()

            coEvery { submissionProcessor.processSubmission(request) } throws RuntimeException("validation error")

            assertThrows<InvalidSubmissionException> { testInstance.createRqt(request) }

            coVerify(exactly = 1) {
                submissionProcessor.processSubmission(request)
                draftService.setProcessingStatus(submission.owner, "TMP_123")
                draftService.setActiveStatus("TMP_123")
            }
            coVerify(exactly = 0) {
                collectionValidationService.executeCollectionValidators(submission)
                submitter.createRqt(capture(extRequestSlot))
                draftService.setAcceptedStatus("TMP_123")
            }
        }

    private fun setUpRequest() {
        every { request.draftKey } returns "TMP_123"
        every { request.owner } returns basicExtSubmission.owner
        every { request.accNo } returns basicExtSubmission.accNo
        every { request.previousVersion } returns null
        every { request.silentMode } returns false
        every { request.processAll } returns true
    }

    private fun setUpDraftService() {
        coEvery { draftService.setAcceptedStatus("TMP_123") } answers { nothing }
        coEvery { draftService.setActiveStatus("TMP_123") } answers { nothing }
        coEvery { draftService.setAcceptedStatus("S-TEST123") } answers { nothing }
        coEvery { draftService.setProcessingStatus(basicExtSubmission.owner, "TMP_123") } answers { nothing }
        coEvery { draftService.deleteSubmissionDraft(basicExtSubmission.submitter, "S-TEST123") } answers { nothing }
    }
}
