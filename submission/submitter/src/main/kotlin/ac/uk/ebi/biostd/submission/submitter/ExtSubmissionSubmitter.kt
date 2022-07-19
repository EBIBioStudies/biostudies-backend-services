package ac.uk.ebi.biostd.submission.submitter

import ac.uk.ebi.biostd.persistence.common.request.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionDraftService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.submission.submitter.request.SubmissionRequestLoader
import ac.uk.ebi.biostd.submission.submitter.request.SubmissionRequestProcessor
import ac.uk.ebi.biostd.submission.submitter.request.SubmissionRequestReleaser
import ebi.ac.uk.extended.model.ExtSubmission

class ExtSubmissionSubmitter(
    private val persistenceService: SubmissionPersistenceService,
    private val draftService: SubmissionDraftService,
    private val requestLoader: SubmissionRequestLoader,
    private val requestProcessor: SubmissionRequestProcessor,
    private val requestReleaser: SubmissionRequestReleaser,
) {
    fun submitAsync(request: SubmissionRequest): Pair<String, Int> {
        return saveRequest(request, request.submission.submitter)
    }

    fun processRequest(accNo: String, version: Int): ExtSubmission {
        val sub = requestLoader.loadRequest(accNo, version)
        val submission = requestProcessor.processRequest(sub)
        if (submission.released) requestReleaser.releaseSubmission(submission)
        return submission
    }

    fun release(sub: ExtSubmission) = requestReleaser.releaseSubmission(sub)

    private fun saveRequest(request: SubmissionRequest, owner: String): Pair<String, Int> {
        val saved = persistenceService.saveSubmissionRequest(request)
        request.draftKey?.let { draftService.setProcessingStatus(owner, it) }
        return saved
    }
}
