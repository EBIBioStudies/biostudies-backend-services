package ac.uk.ebi.biostd.submission.submitter

import ac.uk.ebi.biostd.persistence.common.request.ExtSubmitRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionDraftPersistenceService
import ac.uk.ebi.biostd.submission.exceptions.InvalidSubmissionException
import ac.uk.ebi.biostd.submission.model.SubmitRequest
import ac.uk.ebi.biostd.submission.service.DoiService
import ac.uk.ebi.biostd.submission.validator.collection.CollectionValidationService
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.model.constants.SubFields.DOI_REQUESTED
import ebi.ac.uk.test.basicExtSubmission
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class SubmissionSubmitterTest(
    @MockK private val request: SubmitRequest,
    @MockK private val doiService: DoiService,
    @MockK private val submissionSubmitter: ExtSubmissionSubmitter,
    @MockK private val submissionProcessor: SubmissionProcessor,
    @MockK private val collectionValidationService: CollectionValidationService,
    @MockK private val draftService: SubmissionDraftPersistenceService,
) {
    private val testInstance = SubmissionSubmitter(
        doiService,
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
    fun `create request`() {
        val submission = basicExtSubmission
        val extRequestSlot = slot<ExtSubmitRequest>()

        every { submissionProcessor.processSubmission(request) } returns submission
        every { collectionValidationService.executeCollectionValidators(submission) } answers { nothing }
        every {
            submissionSubmitter.createRequest(capture(extRequestSlot))
        } returns (submission.accNo to submission.version)

        testInstance.createRequest(request)

        val extRequest = extRequestSlot.captured
        assertThat(extRequest.draftKey).isEqualTo("TMP_123")
        assertThat(extRequest.submission).isEqualTo(submission)
        verify(exactly = 1) {
            submissionProcessor.processSubmission(request)
            collectionValidationService.executeCollectionValidators(submission)
            draftService.setProcessingStatus(submission.owner, "TMP_123")
            submissionSubmitter.createRequest(extRequest)
            draftService.setAcceptedStatus("TMP_123")
        }
        verify(exactly = 0) {
            draftService.setActiveStatus("TMP_123")
            doiService.registerDoi(submission)
        }
    }

    @Test
    fun `create request with doi`() {
        val submission = basicExtSubmission.copy(attributes = listOf(ExtAttribute(DOI_REQUESTED.value, "")))
        val extRequestSlot = slot<ExtSubmitRequest>()

        every { doiService.registerDoi(submission) } answers { nothing }
        every { submissionProcessor.processSubmission(request) } returns submission
        every { collectionValidationService.executeCollectionValidators(submission) } answers { nothing }
        every {
            submissionSubmitter.createRequest(capture(extRequestSlot))
        } returns (submission.accNo to submission.version)

        testInstance.createRequest(request)

        val extRequest = extRequestSlot.captured
        assertThat(extRequest.draftKey).isEqualTo("TMP_123")
        assertThat(extRequest.submission).isEqualTo(submission)
        verify(exactly = 1) {
            submissionProcessor.processSubmission(request)
            collectionValidationService.executeCollectionValidators(submission)
            doiService.registerDoi(submission)
            draftService.setProcessingStatus(submission.owner, "TMP_123")
            submissionSubmitter.createRequest(extRequest)
            draftService.setAcceptedStatus("TMP_123")
        }
        verify(exactly = 0) {
            draftService.setActiveStatus("TMP_123")
        }
    }

    @Test
    fun `create request with doi already existing`() {
        val submission = basicExtSubmission.copy(attributes = listOf(ExtAttribute(DOI_REQUESTED.value, "")))
        val extRequestSlot = slot<ExtSubmitRequest>()

        every { request.previousVersion } returns submission
        every { doiService.registerDoi(submission) } answers { nothing }
        every { submissionProcessor.processSubmission(request) } returns submission
        every { collectionValidationService.executeCollectionValidators(submission) } answers { nothing }
        every {
            submissionSubmitter.createRequest(capture(extRequestSlot))
        } returns (submission.accNo to submission.version)

        testInstance.createRequest(request)

        val extRequest = extRequestSlot.captured
        assertThat(extRequest.draftKey).isEqualTo("TMP_123")
        assertThat(extRequest.submission).isEqualTo(submission)
        verify(exactly = 1) {
            submissionProcessor.processSubmission(request)
            collectionValidationService.executeCollectionValidators(submission)
            draftService.setProcessingStatus(submission.owner, "TMP_123")
            submissionSubmitter.createRequest(extRequest)
            draftService.setAcceptedStatus("TMP_123")
        }
        verify(exactly = 0) {
            doiService.registerDoi(submission)
            draftService.setActiveStatus("TMP_123")
        }
    }

    @Test
    fun `create with failure on validation`() {
        val submission = basicExtSubmission
        val extRequestSlot = slot<ExtSubmitRequest>()

        every { submissionProcessor.processSubmission(request) } throws RuntimeException("validation error")

        assertThrows<InvalidSubmissionException> { testInstance.createRequest(request) }

        verify(exactly = 1) {
            submissionProcessor.processSubmission(request)
            draftService.setProcessingStatus(submission.owner, "TMP_123")
            draftService.setActiveStatus("TMP_123")
        }
        verify(exactly = 0) {
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
        every { draftService.setAcceptedStatus("TMP_123") } answers { nothing }
        every { draftService.setActiveStatus("TMP_123") } answers { nothing }
        every { draftService.setAcceptedStatus("S-TEST123") } answers { nothing }
        every { draftService.setProcessingStatus(basicExtSubmission.owner, "TMP_123") } answers { nothing }
        every { draftService.deleteSubmissionDraft(basicExtSubmission.submitter, "S-TEST123") } answers { nothing }
    }
}
