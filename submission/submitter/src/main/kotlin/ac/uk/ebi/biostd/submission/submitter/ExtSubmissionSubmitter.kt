package ac.uk.ebi.biostd.submission.submitter

import ac.uk.ebi.biostd.persistence.common.request.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionDraftService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.submission.model.ReleaseRequest
import ebi.ac.uk.extended.model.ExtSubmission

class ExtSubmissionSubmitter(
    private val persistenceService: SubmissionPersistenceService,
    private val draftService: SubmissionDraftService,
    private val requestProcessor: SubmissionRequestProcessor,
) {
    fun submitAsync(request: SubmissionRequest): Pair<String, Int> {
        return saveRequest(request, request.submission.submitter)
    }

    fun processRequest(accNo: String, version: Int): ExtSubmission {
        val sub = requestProcessor.loadRequest(accNo, version)
        return persistenceService.processSubmissionRequest(sub)
    }

    fun release(request: ReleaseRequest) {
        val (accNo, owner, relPath) = request
        persistenceService.releaseSubmission(accNo, owner, relPath)
    }

    private fun saveRequest(request: SubmissionRequest, owner: String): Pair<String, Int> {
        val saved = persistenceService.saveSubmissionRequest(request)
        request.draftKey?.let { draftService.setProcessingStatus(owner, it) }
        return saved
    }
}
