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
import io.mockk.mockkStatic
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant

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
        setUpTime()
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

            testInstance.processRequestDraft(request)

            val extRequest = extRequestSlot.captured
            assertThat(extRequest.draftKey).isEqualTo(DRAFT_KEY)
            assertThat(extRequest.draftContent).isEqualTo(DRAFT_CONTENT)
            assertThat(extRequest.submission).isEqualTo(sub)
            coVerify(exactly = 1) {
                submissionProcessor.processSubmission(request)
                collectionValidationService.executeCollectionValidators(sub)
                draftService.setProcessingStatus(sub.owner, DRAFT_KEY, MODIFICATION_TIME)
                submitter.createRqt(extRequest)
                draftService.setAcceptedStatus(DRAFT_KEY, MODIFICATION_TIME)
            }
            coVerify(exactly = 0) {
                draftService.setActiveStatus(any(), any())
            }
        }

    @Test
    fun `create with failure on validation`() =
        runTest {
            val submission = basicExtSubmission

            coEvery { submissionProcessor.processSubmission(request) } throws RuntimeException("validation error")

            assertThrows<InvalidSubmissionException> { testInstance.processRequestDraft(request) }

            coVerify(exactly = 1) {
                submissionProcessor.processSubmission(request)
                draftService.setActiveStatus(DRAFT_KEY, MODIFICATION_TIME)
                draftService.setProcessingStatus(submission.owner, DRAFT_KEY, MODIFICATION_TIME)
            }
            coVerify(exactly = 0) {
                submitter.createRqt(any())
                draftService.setAcceptedStatus(any(), any())
                collectionValidationService.executeCollectionValidators(any())
            }
        }

    private fun setUpTime() {
        mockkStatic(Instant::class)
        coEvery { Instant.now() } returns MODIFICATION_TIME
    }

    private fun setUpRequest() {
        every { request.draftKey } returns DRAFT_KEY
        every { request.draftContent } returns DRAFT_CONTENT
        every { request.owner } returns basicExtSubmission.owner
        every { request.accNo } returns basicExtSubmission.accNo
        every { request.previousVersion } returns null
        every { request.silentMode } returns false
        every { request.singleJobMode } returns true
    }

    private fun setUpDraftService() {
        coEvery { draftService.setAcceptedStatus(ACC_NO, MODIFICATION_TIME) } answers { nothing }
        coEvery { draftService.setActiveStatus(DRAFT_KEY, MODIFICATION_TIME) } answers { nothing }
        coEvery { draftService.setAcceptedStatus(DRAFT_KEY, MODIFICATION_TIME) } answers { nothing }
        coEvery { draftService.deleteSubmissionDraft(basicExtSubmission.submitter, ACC_NO) } answers { nothing }
        coEvery {
            draftService.setProcessingStatus(basicExtSubmission.owner, DRAFT_KEY, MODIFICATION_TIME)
        } answers { nothing }
    }

    companion object {
        private const val ACC_NO = "S-TEST123"
        private const val DRAFT_CONTENT = "submission-draft"
        private const val DRAFT_KEY = "TMP_1970-01-01T00:00:00.002Z"
        private val MODIFICATION_TIME = Instant.ofEpochMilli(2)
    }
}
