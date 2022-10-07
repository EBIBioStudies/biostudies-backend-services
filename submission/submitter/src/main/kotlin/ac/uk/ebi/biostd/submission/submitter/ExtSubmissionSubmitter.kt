package ac.uk.ebi.biostd.submission.submitter

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.CLEANED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.FILES_COPIED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.LOADED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.REQUESTED
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.request.ExtSubmitRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.submission.submitter.request.SubmissionRequestCleaner
import ac.uk.ebi.biostd.submission.submitter.request.SubmissionRequestReleaser
import ac.uk.ebi.biostd.submission.submitter.request.SubmissionRequestLoader
import ac.uk.ebi.biostd.submission.submitter.request.SubmissionRequestProcessor
import ebi.ac.uk.extended.model.ExtSubmission

@Suppress("LongParameterList", "TooManyFunctions")
class ExtSubmissionSubmitter(
    private val requestService: SubmissionRequestPersistenceService,
    private val persistenceService: SubmissionPersistenceService,
    private val requestLoader: SubmissionRequestLoader,
    private val requestProcessor: SubmissionRequestProcessor,
    private val requestReleaser: SubmissionRequestReleaser,
    private val requestCleaner: SubmissionRequestCleaner,
) {
    fun createRequest(rqt: ExtSubmitRequest): Pair<String, Int> {
        val submission = rqt.submission.copy(version = persistenceService.getNextVersion(rqt.submission.accNo))
        val request = SubmissionRequest(submission, rqt.draftKey, REQUESTED)

        return requestService.createSubmissionRequest(request)
    }

    fun loadRequest(accNo: String, version: Int): ExtSubmission = requestLoader.loadRequest(accNo, version)

    fun cleanRequest(accNo: String, version: Int) = requestCleaner.cleanCurrentVersion(accNo, version)

    fun processRequest(accNo: String, version: Int): ExtSubmission = requestProcessor.processRequest(accNo, version)

    fun checkReleased(accNo: String, version: Int): ExtSubmission = requestReleaser.checkReleased(accNo, version)

    fun release(accNo: String) = requestReleaser.releaseSubmission(accNo)

    fun handleRequest(accNo: String, version: Int): ExtSubmission {
        return when (requestService.getRequestStatus(accNo, version)) {
            REQUESTED -> completeRequest(accNo, version)
            LOADED -> processRequestFiles(accNo, version)
            CLEANED -> processCleanedFiles(accNo, version)
            FILES_COPIED -> requestReleaser.checkReleased(accNo, version)
            else -> throw IllegalStateException("Request accNo=$accNo, version='$version' has been already processed")
        }
    }

    private fun completeRequest(accNo: String, version: Int): ExtSubmission {
        loadRequest(accNo, version)
        cleanRequest(accNo, version)
        processRequest(accNo, version)
        return checkReleased(accNo, version)
    }

    private fun processRequestFiles(accNo: String, version: Int): ExtSubmission {
        cleanRequest(accNo, version)
        processRequest(accNo, version)
        return checkReleased(accNo, version)
    }

    private fun processCleanedFiles(accNo: String, version: Int): ExtSubmission {
        processRequest(accNo, version)
        return checkReleased(accNo, version)
    }
}
