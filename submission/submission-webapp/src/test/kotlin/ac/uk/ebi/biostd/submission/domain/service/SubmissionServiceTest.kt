package ac.uk.ebi.biostd.submission.domain.service

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.persistence.common.request.SaveSubmissionRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionMetaQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.submission.model.SubmissionRequest
import ac.uk.ebi.biostd.submission.submitter.SubmissionSubmitter
import ebi.ac.uk.extended.events.FailedSubmissionRequestMessage
import ebi.ac.uk.extended.events.SubmissionRequestMessage
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FileMode.COPY
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.amqp.rabbit.core.RabbitTemplate
import uk.ac.ebi.events.config.BIOSTUDIES_EXCHANGE
import uk.ac.ebi.events.config.SUBMISSIONS_REQUEST_ROUTING_KEY
import uk.ac.ebi.events.service.EventsPublisherService

@ExtendWith(MockKExtension::class)
class SubmissionServiceTest(
    @MockK private val submissionQueryService: SubmissionQueryService,
    @MockK private val serializationService: SerializationService,
    @MockK private val userPrivilegesService: IUserPrivilegesService,
    @MockK private val queryService: SubmissionMetaQueryService,
    @MockK private val submissionSubmitter: SubmissionSubmitter,
    @MockK private val eventsPublisherService: EventsPublisherService,
    @MockK private val rabbitTemplate: RabbitTemplate
) {
    private val testInstance = SubmissionService(
        submissionQueryService,
        serializationService,
        userPrivilegesService,
        queryService,
        submissionSubmitter,
        eventsPublisherService,
        rabbitTemplate
    )

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun submit(
        @MockK extSubmission: ExtSubmission,
        @MockK submissionRequest: SubmissionRequest
    ) {
        every { submissionRequest.accNo } returns "S-BSST1"
        every { extSubmission.submitter } returns "test@ebi.ac.uk"
        every { submissionSubmitter.submit(submissionRequest) } returns extSubmission
        every { eventsPublisherService.submissionSubmitted(extSubmission) } answers { nothing }

        val submission = testInstance.submit(submissionRequest)
        assertThat(submission).isEqualTo(extSubmission)
        verify(exactly = 1) { eventsPublisherService.submissionSubmitted(extSubmission) }
    }

    @Test
    fun submitAsync(
        @MockK extSubmission: ExtSubmission,
        @MockK submissionRequest: SubmissionRequest
    ) {
        val requestMessageSlot = slot<SubmissionRequestMessage>()
        val saveSubmissionRequest = SaveSubmissionRequest(extSubmission, COPY, "TMP_123456")

        every { extSubmission.version } returns 1
        every { extSubmission.accNo } returns "S-BSST1"
        every { submissionRequest.accNo } returns "S-BSST1"
        every { extSubmission.submitter } returns "test@ebi.ac.uk"
        every { submissionSubmitter.submitAsync(submissionRequest) } returns saveSubmissionRequest
        every {
            rabbitTemplate.convertAndSend(
                BIOSTUDIES_EXCHANGE,
                SUBMISSIONS_REQUEST_ROUTING_KEY,
                capture(requestMessageSlot)
            )
        } answers { nothing }

        testInstance.submitAsync(submissionRequest)
        val requestMessage = requestMessageSlot.captured

        assertAsyncRequestMessage(requestMessage)
        verify(exactly = 1) {
            submissionSubmitter.submitAsync(submissionRequest)
            rabbitTemplate.convertAndSend(BIOSTUDIES_EXCHANGE, SUBMISSIONS_REQUEST_ROUTING_KEY, requestMessage)
        }
    }

    private fun assertAsyncRequestMessage(requestMessage: SubmissionRequestMessage) {
        assertThat(requestMessage.accNo).isEqualTo("S-BSST1")
        assertThat(requestMessage.version).isEqualTo(1)
        assertThat(requestMessage.fileMode).isEqualTo(COPY)
        assertThat(requestMessage.draftKey).isEqualTo("TMP_123456")
    }

    @Test
    fun `process submission`(
        @MockK extSubmission: ExtSubmission
    ) {
        val saveRequestSlot = slot<SaveSubmissionRequest>()
        val message = SubmissionRequestMessage("S-BSST1", 1, COPY, "user@mail.org", "TMP_123")

        every { submissionQueryService.getRequest("S-BSST1", 1) } returns extSubmission
        every { eventsPublisherService.submissionSubmitted(extSubmission) } answers { nothing }
        every { submissionSubmitter.processRequest(capture(saveRequestSlot)) } returns extSubmission

        testInstance.processSubmission(message)

        val saveRequest = saveRequestSlot.captured
        assertSaveRequest(saveRequest, extSubmission)

        verify(exactly = 0) { eventsPublisherService.submissionFailed(any()) }
        verify(exactly = 1) {
            submissionQueryService.getRequest("S-BSST1", 1)
            submissionSubmitter.processRequest(saveRequest)
            eventsPublisherService.submissionSubmitted(extSubmission)
        }
    }

    @Test
    fun `process failed submission`(
        @MockK extSubmission: ExtSubmission
    ) {
        val saveRequestSlot = slot<SaveSubmissionRequest>()
        val failedMessageSlot = slot<FailedSubmissionRequestMessage>()
        val message = SubmissionRequestMessage("S-BSST1", 1, COPY, "user@mail.org", "TMP_123")

        every { submissionQueryService.getRequest("S-BSST1", 1) } returns extSubmission
        every { eventsPublisherService.submissionFailed(capture(failedMessageSlot)) } answers { nothing }
        every { submissionSubmitter.processRequest(capture(saveRequestSlot)) } throws Exception("an error message")

        testInstance.processSubmission(message)

        val saveRequest = saveRequestSlot.captured
        assertSaveRequest(saveRequest, extSubmission)

        val failedMessage = failedMessageSlot.captured
        assertFailedMessage(failedMessage)

        verify(exactly = 0) { eventsPublisherService.submissionSubmitted(extSubmission) }
        verify(exactly = 1) {
            submissionQueryService.getRequest("S-BSST1", 1)
            submissionSubmitter.processRequest(saveRequest)
            eventsPublisherService.submissionFailed(failedMessage)
        }
    }

    private fun assertSaveRequest(saveRequest: SaveSubmissionRequest, extSubmission: ExtSubmission) {
        assertThat(saveRequest.fileMode).isEqualTo(COPY)
        assertThat(saveRequest.draftKey).isEqualTo("TMP_123")
        assertThat(saveRequest.submission).isEqualTo(extSubmission)
    }

    private fun assertFailedMessage(failedMessage: FailedSubmissionRequestMessage) {
        assertThat(failedMessage.version).isEqualTo(1)
        assertThat(failedMessage.fileMode).isEqualTo(COPY)
        assertThat(failedMessage.accNo).isEqualTo("S-BSST1")
        assertThat(failedMessage.draftKey).isEqualTo("TMP_123")
        assertThat(failedMessage.errorMessage).isEqualTo("an error message")
    }
}
