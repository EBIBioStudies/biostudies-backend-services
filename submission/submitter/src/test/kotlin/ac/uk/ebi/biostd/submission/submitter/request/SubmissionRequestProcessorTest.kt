package ac.uk.ebi.biostd.submission.submitter.request

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.CLEANED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.FILES_COPIED
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
//import ac.uk.ebi.biostd.persistence.filesystem.service.FileSystemService
import ebi.ac.uk.test.basicExtSubmission
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class SubmissionRequestProcessorTest(
//    @MockK private val systemService: FileSystemService,
    @MockK private val queryService: SubmissionPersistenceQueryService,
    @MockK private val persistenceService: SubmissionPersistenceService,
) {
    private val testInstance = SubmissionRequestProcessor(systemService, queryService, persistenceService)

    @Test
    fun `process request`() {
        val submission = basicExtSubmission
        val processedRequestSlot = slot<SubmissionRequest>()
        val cleanedRequest = SubmissionRequest(submission, "TMP_123", CLEANED)

        every { persistenceService.saveSubmission(submission) } returns submission
        every { systemService.persistSubmissionFiles(submission) } returns submission
        every { queryService.getCleanedRequest(submission.accNo, 1) } returns cleanedRequest
        every { persistenceService.expirePreviousVersions(submission.accNo) } answers { nothing }
        every {
            persistenceService.saveSubmissionRequest(capture(processedRequestSlot))
        } returns (submission.accNo to submission.version)

        val processed = testInstance.processRequest(submission.accNo, submission.version)
        val processedRequest = processedRequestSlot.captured

        assertThat(processed).isEqualTo(submission)
        assertThat(processedRequest.status).isEqualTo(FILES_COPIED)
        verify(exactly = 1) {
            systemService.persistSubmissionFiles(submission)
            persistenceService.expirePreviousVersions(submission.accNo)
            persistenceService.saveSubmission(processed)
            persistenceService.saveSubmissionRequest(processedRequest)
        }
    }
}
