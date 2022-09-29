package ac.uk.ebi.biostd.submission.submitter.request

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.CLEANED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.LOADED
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.service.FileSystemService
import ebi.ac.uk.test.basicExtSubmission
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

@ExtendWith(MockKExtension::class)
class SubmissionCleanerTest(
    @MockK private val systemService: FileSystemService,
    @MockK private val queryService: SubmissionPersistenceQueryService,
    @MockK private val persistenceService: SubmissionPersistenceService,
) {
    private val testInstance = SubmissionCleaner(systemService, queryService, persistenceService)

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun `clean current version`() {
        val submission = basicExtSubmission
        val cleanedRequestSlot = slot<SubmissionRequest>()
        val loadedRequest = SubmissionRequest(submission, "TMP_123", LOADED)

        every { systemService.cleanSubmissionFiles(submission) } answers { nothing }
        every { queryService.getLoadedRequest("S-BSST0", 1) } returns loadedRequest
        every { queryService.findExtByAccNo("S-BSST0", includeFileListFiles = true) } returns submission
        every {
            persistenceService.saveSubmissionRequest(capture(cleanedRequestSlot))
        } returns (submission.accNo to submission.version)

        testInstance.cleanCurrentVersion("S-BSST0", 1)

        val cleanedRequest = cleanedRequestSlot.captured
        assertThat(cleanedRequest.status).isEqualTo(CLEANED)
        verify(exactly = 1) {
            systemService.cleanSubmissionFiles(submission)
            queryService.getLoadedRequest("S-BSST0", 1)
            persistenceService.saveSubmissionRequest(cleanedRequest)
            queryService.findExtByAccNo("S-BSST0", includeFileListFiles = true)
        }
    }

    @Test
    fun `clean current version when no previous version is found`() {
        val submission = basicExtSubmission
        val cleanedRequestSlot = slot<SubmissionRequest>()
        val loadedRequest = SubmissionRequest(submission, "TMP_123", LOADED)

        every { queryService.getLoadedRequest("S-BSST0", 1) } returns loadedRequest
        every { queryService.findExtByAccNo("S-BSST0", includeFileListFiles = true) } returns null
        every {
            persistenceService.saveSubmissionRequest(capture(cleanedRequestSlot))
        } returns (submission.accNo to submission.version)

        testInstance.cleanCurrentVersion("S-BSST0", 1)

        val cleanedRequest = cleanedRequestSlot.captured
        assertThat(cleanedRequest.status).isEqualTo(CLEANED)
        verify(exactly = 0) { systemService.cleanSubmissionFiles(any()) }
        verify(exactly = 1) {
            queryService.getLoadedRequest("S-BSST0", 1)
            persistenceService.saveSubmissionRequest(cleanedRequest)
            queryService.findExtByAccNo("S-BSST0", includeFileListFiles = true)
        }
    }
}
