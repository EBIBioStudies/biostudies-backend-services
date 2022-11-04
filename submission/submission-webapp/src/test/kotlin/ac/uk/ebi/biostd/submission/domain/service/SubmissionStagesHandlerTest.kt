package ac.uk.ebi.biostd.submission.domain.service

import ac.uk.ebi.biostd.submission.submitter.SubmissionSubmitter
import ebi.ac.uk.extended.events.RequestCreated
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.events.service.EventsPublisherService

@ExtendWith(MockKExtension::class)
class SubmissionStagesHandlerTest(
    @MockK private val submissionSubmitter: SubmissionSubmitter,
    @MockK private val eventsPublisherService: EventsPublisherService,
) {
    private val testInstance = SubmissionStagesHandler(submissionSubmitter, eventsPublisherService)

    @Test
    fun `index request`() {
        val request = RequestCreated("S-BSTT0", 1)

        every { submissionSubmitter.indexRequest(request) } answers { nothing }
        every { eventsPublisherService.requestIndexed("S-BSTT0", 1) } answers { nothing }

        testInstance.indexRequest(request)

        verify(exactly = 1) {
            submissionSubmitter.indexRequest(request)
            eventsPublisherService.requestIndexed("S-BSTT0", 1)
        }
    }
}
