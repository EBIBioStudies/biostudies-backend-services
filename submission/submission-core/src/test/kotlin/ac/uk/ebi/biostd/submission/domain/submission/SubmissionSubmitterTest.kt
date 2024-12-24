package ac.uk.ebi.biostd.submission.domain.submission

import ac.uk.ebi.biostd.persistence.common.request.ExtSubmitRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.submission.domain.submitter.ExtSubmissionSubmitter
import ac.uk.ebi.biostd.submission.exceptions.InvalidSubmissionException
import ac.uk.ebi.biostd.submission.model.SubmitRequest
import ac.uk.ebi.biostd.submission.validator.collection.CollectionValidationService
import ebi.ac.uk.asserts.assertThrows
import ebi.ac.uk.model.RequestStatus.DRAFT
import ebi.ac.uk.model.RequestStatus.SUBMITTED
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
import ebi.ac.uk.test.basicExtSubmission as sub

@ExtendWith(MockKExtension::class)
class SubmissionSubmitterTest(
    @MockK private val request: SubmitRequest,
    @MockK private val submitter: ExtSubmissionSubmitter,
    @MockK private val submissionProcessor: SubmissionProcessor,
    @MockK private val collectionValidationService: CollectionValidationService,
    @MockK private val requestService: SubmissionRequestPersistenceService,
) {
    private val testInstance =
        SubmissionSubmitter(
            submitter,
            submissionProcessor,
            collectionValidationService,
            requestService,
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
            val extRequestSlot = slot<ExtSubmitRequest>()

            coEvery { requestService.hasActiveRequest(sub.accNo) } returns false
            coEvery { submissionProcessor.processSubmission(request) } returns sub
            coEvery { collectionValidationService.executeCollectionValidators(sub) } answers { nothing }
            coEvery { submitter.createRqt(capture(extRequestSlot)) } returns (sub.accNo to sub.version)

            testInstance.processRequestDraft(request)

            val extRequest = extRequestSlot.captured
            assertThat(extRequest.key).isEqualTo(RQT_KEY)
            assertThat(extRequest.submission).isEqualTo(sub)
            coVerify(exactly = 1) {
                submissionProcessor.processSubmission(request)
                collectionValidationService.executeCollectionValidators(sub)
                requestService.setDraftStatus(RQT_KEY, sub.owner, SUBMITTED, MODIFICATION_TIME)
                submitter.createRqt(extRequest)
            }
            coVerify(exactly = 0) {
                requestService.setDraftStatus(RQT_KEY, sub.owner, DRAFT, MODIFICATION_TIME)
            }
        }

    @Test
    fun `create with failure on validation`() =
        runTest {
            coEvery { submissionProcessor.processSubmission(request) } throws RuntimeException("validation error")

            assertThrows<InvalidSubmissionException> { testInstance.processRequestDraft(request) }

            coVerify(exactly = 1) {
                submissionProcessor.processSubmission(request)
                requestService.setDraftStatus(RQT_KEY, sub.owner, SUBMITTED, MODIFICATION_TIME)
                requestService.setDraftStatus(RQT_KEY, sub.owner, DRAFT, MODIFICATION_TIME)
            }
            coVerify(exactly = 0) {
                submitter.createRqt(any())
                collectionValidationService.executeCollectionValidators(any())
            }
        }

    private fun setUpTime() {
        mockkStatic(Instant::class)
        coEvery { Instant.now() } returns MODIFICATION_TIME
    }

    private fun setUpRequest() {
        every { request.accNo } returns RQT_KEY
        every { request.owner } returns sub.owner
        every { request.accNo } returns sub.accNo
        every { request.previousVersion } returns null
        every { request.silentMode } returns false
        every { request.singleJobMode } returns true
    }

    private fun setUpDraftService() {
        coEvery { requestService.setDraftStatus(RQT_KEY, sub.owner, DRAFT, MODIFICATION_TIME) } answers { nothing }
        coEvery { requestService.setDraftStatus(RQT_KEY, sub.owner, SUBMITTED, MODIFICATION_TIME) } answers { nothing }
    }

    companion object {
        private const val RQT_KEY = "TMP_1970-01-01T00:00:00.002Z"
        private val MODIFICATION_TIME = Instant.ofEpochMilli(2)
    }
}
