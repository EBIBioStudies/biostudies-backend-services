package ac.uk.ebi.biostd.submission.domain.service

import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.api.FileStorageService
import ac.uk.ebi.biostd.submission.submitter.ExtSubmissionSubmitter
import ac.uk.ebi.biostd.submission.submitter.SubmissionSubmitter
import ebi.ac.uk.extended.events.RequestCreated
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.events.service.EventsPublisherService

@ExtendWith(MockKExtension::class)
class SubmissionServiceTest(
    @MockK private val queryService: SubmissionPersistenceQueryService,
    @MockK private val userPrivilegesService: IUserPrivilegesService,
    @MockK private val extSubmissionSubmitter: ExtSubmissionSubmitter,
    @MockK private val submissionSubmitter: SubmissionSubmitter,
    @MockK private val eventsPublisherService: EventsPublisherService,
    @MockK private val fileStorageService: FileStorageService,
    @MockK private val submissionPersistenceService: SubmissionPersistenceService,
) {
    private val testInstance = SubmissionService(
        queryService,
        userPrivilegesService,
        extSubmissionSubmitter,
        submissionSubmitter,
        eventsPublisherService,
        fileStorageService,
        submissionPersistenceService,
    )

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
