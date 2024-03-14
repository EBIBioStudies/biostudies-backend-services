package ac.uk.ebi.biostd.submission.domain.service

import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService

class SubmissionRequestService(
    private val requestPersistenceService: SubmissionRequestPersistenceService,
) {
    suspend fun getSubmissionRequest(accNo: String, version: Int): SubmissionRequest {
        return requestPersistenceService.getSubmissionRequest(accNo, version)
    }
}
