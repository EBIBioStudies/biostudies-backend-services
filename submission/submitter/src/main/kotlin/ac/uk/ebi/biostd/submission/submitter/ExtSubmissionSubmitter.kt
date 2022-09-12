package ac.uk.ebi.biostd.submission.submitter

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.FILES_COPIED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.LOADED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.REQUESTED
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.request.ExtSubmitRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.submission.submitter.request.SubmissionReleaser
import ac.uk.ebi.biostd.submission.submitter.request.SubmissionRequestLoader
import ac.uk.ebi.biostd.submission.submitter.request.SubmissionRequestProcessor
import ebi.ac.uk.extended.model.ExtSubmission

class ExtSubmissionSubmitter(
    private val queryService: SubmissionPersistenceQueryService,
    private val persistenceService: SubmissionPersistenceService,
    private val requestLoader: SubmissionRequestLoader,
    private val requestProcessor: SubmissionRequestProcessor,
    private val requestReleaser: SubmissionReleaser,
) {
    fun saveRequest(rqt: ExtSubmitRequest): Pair<String, Int> {
        val submission = rqt.submission.copy(version = persistenceService.getNextVersion(rqt.submission.accNo))
        return persistenceService.createSubmissionRequest(SubmissionRequest(submission, rqt.draftKey, REQUESTED))
    }

    fun loadRequest(accNo: String, version: Int): ExtSubmission = requestLoader.loadRequest(accNo, version)

    fun processRequest(accNo: String, version: Int): ExtSubmission = requestProcessor.processRequest(accNo, version)

    fun checkReleased(accNo: String, version: Int): ExtSubmission = requestReleaser.checkReleased(accNo, version)

    fun release(accNo: String) = requestReleaser.releaseSubmission(accNo)

    fun handleRequest(accNo: String, version: Int): ExtSubmission {
        return when (queryService.getRequestStatus(accNo, version)) {
            REQUESTED -> completeRequest(accNo, version)
            LOADED -> processRequestFiles(accNo, version)
            FILES_COPIED -> requestReleaser.checkReleased(accNo, version)
            else -> throw IllegalStateException("Request accNo=$accNo, version='$version' has been already processed")
        }
    }

    private fun completeRequest(accNo: String, version: Int): ExtSubmission {
        requestLoader.loadRequest(accNo, version)
        requestProcessor.processRequest(accNo, version)
        return requestReleaser.checkReleased(accNo, version)
    }

    private fun processRequestFiles(accNo: String, version: Int): ExtSubmission {
        requestProcessor.processRequest(accNo, version)
        return requestReleaser.checkReleased(accNo, version)
    }
}
