package ac.uk.ebi.biostd.submission.domain.service

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionMetaQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.submission.exceptions.ConcurrentProcessingSubmissionException
import ac.uk.ebi.biostd.submission.model.SubmissionRequest
import ac.uk.ebi.biostd.submission.submitter.SubmissionSubmitter
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.amqp.rabbit.core.RabbitTemplate
import uk.ac.ebi.events.service.EventsPublisherService

@ExtendWith(MockKExtension::class)
class SubmissionServiceTest(
    @MockK private val subRepository: SubmissionQueryService,
    @MockK private val serializationService: SerializationService,
    @MockK private val userPrivilegesService: IUserPrivilegesService,
    @MockK private val queryService: SubmissionMetaQueryService,
    @MockK private val submissionSubmitter: SubmissionSubmitter,
    @MockK private val eventsPublisherService: EventsPublisherService,
    @MockK private val rabbitTemplate: RabbitTemplate
) {
    private val testInstance = SubmissionService(
        subRepository,
        serializationService,
        userPrivilegesService,
        queryService,
        submissionSubmitter,
        eventsPublisherService,
        rabbitTemplate)

    @Test
    fun submit(
        @MockK extSubmission: ExtSubmission,
        @MockK submissionRequest: SubmissionRequest
    ) {
        every { extSubmission.submitter } returns "test@ebi.ac.uk"
        every { submissionSubmitter.submit(submissionRequest) } returns extSubmission
        every { eventsPublisherService.submissionSubmitted(extSubmission) } answers { nothing }

        val submission = testInstance.submit(submissionRequest)
        assertThat(submission).isEqualTo(extSubmission)
        verify(exactly = 1) { eventsPublisherService.submissionSubmitted(extSubmission) }
    }

    @Test
    fun `require not processing when concurrent`() {
        every { queryService.isProcessing("S-TEST123") } returns true

        val exception = assertThrows<ConcurrentProcessingSubmissionException> {
            testInstance.requireNotProcessing("S-TEST123")
        }

        assertThat(exception.message).isEqualTo(
            "Submission request can't be accepted. Another version for 'S-TEST123' is currently being processed.")
    }

    @Test
    fun `require not processing when not concurrent`() {
        every { queryService.isProcessing("S-TEST123") } returns false
        assertDoesNotThrow { testInstance.requireNotProcessing("S-TEST123") }
    }
}
