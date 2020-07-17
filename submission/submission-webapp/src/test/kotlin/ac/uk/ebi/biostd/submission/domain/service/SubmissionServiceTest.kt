package ac.uk.ebi.biostd.submission.domain.service

import ac.uk.ebi.biostd.events.EventsService
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.persistence.integration.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.service.SubmissionRepository
import ac.uk.ebi.biostd.submission.model.SubmissionRequest
import ac.uk.ebi.biostd.submission.submitter.SubmissionSubmitter
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import ebi.ac.uk.security.integration.model.api.SecurityUser
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class SubmissionServiceTest(
    @MockK private val subRepository: SubmissionRepository,
    @MockK private val serializationService: SerializationService,
    @MockK private val userPrivilegesService: IUserPrivilegesService,
    @MockK private val queryService: SubmissionQueryService,
    @MockK private val submissionSubmitter: SubmissionSubmitter,
    @MockK private val eventsService: EventsService
) {
    private val testInstance = SubmissionService(
        subRepository, serializationService, userPrivilegesService, queryService, submissionSubmitter, eventsService)

    @Test
    fun submit(
        @MockK submitter: SecurityUser,
        @MockK extSubmission: ExtSubmission,
        @MockK submissionRequest: SubmissionRequest
    ) {
        every { submissionRequest.onBehalfUser } returns null
        every { submissionRequest.submitter } returns submitter
        every { submissionSubmitter.submit(submissionRequest) } returns extSubmission
        every { eventsService.submissionSubmitted(extSubmission, submitter) } answers { nothing }

        val submission = testInstance.submit(submissionRequest)
        assertThat(submission).isEqualTo(extSubmission)
        verify(exactly = 1) { eventsService.submissionSubmitted(extSubmission, submitter) }
    }
}
