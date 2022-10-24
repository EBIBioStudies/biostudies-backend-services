package ac.uk.ebi.biostd.submission.submitter.request

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.CLEANED
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.service.StorageService
import ebi.ac.uk.extended.model.ExtSubmission
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class SubmissionRequestCleanerTest(
    @MockK private val storageService: StorageService,
    @MockK private val queryService: SubmissionPersistenceQueryService,
    @MockK private val requestService: SubmissionRequestPersistenceService,
) {
    private val testInstance = SubmissionRequestCleaner(storageService, queryService, requestService)

    @Test
    fun `clean current version`(
        @MockK loadRequest: SubmissionRequest,
        @MockK cleanedRequest: SubmissionRequest,
        @MockK sub: ExtSubmission,
        @MockK requestSub: ExtSubmission,
    ) {
        every { storageService.cleanSubmissionFiles(sub, requestSub) } answers { nothing }
        every { requestService.getLoadedRequest(accNo, version) } returns loadRequest
        every { loadRequest.withStatus(status = CLEANED) } returns cleanedRequest
        every { loadRequest.submission } returns requestSub
        every { queryService.findExtByAccNo(accNo, true) } returns sub
        every { requestService.saveSubmissionRequest(cleanedRequest) } returns (accNo to version)

        testInstance.cleanCurrentVersion(accNo, version)

        verify { storageService.cleanSubmissionFiles(sub, requestSub) }
        verify { requestService.saveSubmissionRequest(cleanedRequest) }
    }

    @Test
    fun `clean current version when no current`(
        @MockK loadRequest: SubmissionRequest,
        @MockK cleanedRequest: SubmissionRequest,
        @MockK requestSub: ExtSubmission,
    ) {
        every { requestService.getLoadedRequest(accNo, version) } returns loadRequest
        every { loadRequest.withStatus(status = CLEANED) } returns cleanedRequest
        every { loadRequest.submission } returns requestSub
        every { queryService.findExtByAccNo(accNo, true) } returns null
        every { requestService.saveSubmissionRequest(cleanedRequest) } returns (accNo to version)

        testInstance.cleanCurrentVersion(accNo, 1)

        verify { requestService.saveSubmissionRequest(cleanedRequest) }
    }

    private companion object {
        const val accNo = "S-BSST0"
        const val version = 1
    }
}
