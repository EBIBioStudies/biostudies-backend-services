package ac.uk.ebi.biostd.submission.domain.service

import ac.uk.ebi.biostd.submission.submitter.SubmissionSubmitter
import ebi.ac.uk.extended.events.RequestCheckedReleased
import ebi.ac.uk.extended.events.RequestCleaned
import ebi.ac.uk.extended.events.RequestCreated
import ebi.ac.uk.extended.events.RequestFilesCopied
import ebi.ac.uk.extended.events.RequestIndexed
import ebi.ac.uk.extended.events.RequestLoaded
import ebi.ac.uk.extended.model.ExtSubmission
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.events.service.EventsPublisherService

@ExtendWith(MockKExtension::class)
class SubmissionStagesHandlerTest(
    @MockK private val submissionSubmitter: SubmissionSubmitter,
    @MockK private val eventsPublisherService: EventsPublisherService,
) {
    private val testInstance = SubmissionStagesHandler(submissionSubmitter, eventsPublisherService)

    @AfterEach
    fun afterEach() = clearAllMocks()

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

    @Test
    fun `load request`() {
        val request = RequestIndexed("S-BSTT0", 1)

        every { submissionSubmitter.loadRequest(request) } answers { nothing }
        every { eventsPublisherService.requestLoaded("S-BSTT0", 1) } answers { nothing }

        testInstance.loadRequest(request)

        verify(exactly = 1) {
            submissionSubmitter.loadRequest(request)
            eventsPublisherService.requestLoaded("S-BSTT0", 1)
        }
    }

    @Test
    fun `clean request`() {
        val request = RequestLoaded("S-BSTT0", 1)

        every { submissionSubmitter.cleanRequest(request) } answers { nothing }
        every { eventsPublisherService.requestCleaned("S-BSTT0", 1) } answers { nothing }

        testInstance.cleanRequest(request)

        verify(exactly = 1) {
            submissionSubmitter.cleanRequest(request)
            eventsPublisherService.requestCleaned("S-BSTT0", 1)
        }
    }

    @Test
    fun `copy request files`() {
        val request = RequestCleaned("S-BSTT0", 1)

        every { submissionSubmitter.processRequest(request) } answers { nothing }
        every { eventsPublisherService.requestFilesCopied("S-BSTT0", 1) } answers { nothing }

        testInstance.copyRequestFiles(request)

        verify(exactly = 1) {
            submissionSubmitter.processRequest(request)
            eventsPublisherService.requestFilesCopied("S-BSTT0", 1)
        }
    }

    @Test
    fun `check released`() {
        val request = RequestFilesCopied("S-BSTT0", 1)

        every { submissionSubmitter.checkReleased(request) } answers { nothing }
        every { eventsPublisherService.checkReleased("S-BSTT0", 1) } answers { nothing }

        testInstance.checkReleased(request)

        verify(exactly = 1) {
            submissionSubmitter.checkReleased(request)
            eventsPublisherService.checkReleased("S-BSTT0", 1)
        }
    }

    @Test
    fun `save submission`(
        @MockK submission: ExtSubmission
    ) {
        val request = RequestCheckedReleased("S-BSTT0", 1)

        every { submission.accNo } returns "S-BSST0"
        every { submission.owner } returns "owner@test.org"
        every { submissionSubmitter.saveRequest(request) } returns submission
        every { eventsPublisherService.submissionSubmitted("S-BSST0", "owner@test.org") } answers { nothing }

        testInstance.saveSubmission(request)

        verify(exactly = 1) {
            submissionSubmitter.saveRequest(request)
            eventsPublisherService.submissionSubmitted("S-BSST0", "owner@test.org")
        }
    }
}
