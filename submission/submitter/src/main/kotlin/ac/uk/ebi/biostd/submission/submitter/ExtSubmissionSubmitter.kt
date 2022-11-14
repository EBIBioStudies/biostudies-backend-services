package ac.uk.ebi.biostd.submission.submitter

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.CLEANED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.FILES_COPIED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.INDEXED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.LOADED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.REQUESTED
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.request.ExtSubmitRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.pagetab.PageTabService
import ac.uk.ebi.biostd.submission.submitter.request.SubmissionRequestCleaner
import ac.uk.ebi.biostd.submission.submitter.request.SubmissionRequestIndexer
import ac.uk.ebi.biostd.submission.submitter.request.SubmissionRequestLoader
import ac.uk.ebi.biostd.submission.submitter.request.SubmissionRequestProcessor
import ac.uk.ebi.biostd.submission.submitter.request.SubmissionRequestReleaser
import ebi.ac.uk.extended.model.ExtSubmission
import java.time.OffsetDateTime

@Suppress("LongParameterList", "TooManyFunctions")
class ExtSubmissionSubmitter(
    private val pageTabService: PageTabService,
    private val requestService: SubmissionRequestPersistenceService,
    private val persistenceService: SubmissionPersistenceService,
    private val requestIndexer: SubmissionRequestIndexer,
    private val requestLoader: SubmissionRequestLoader,
    private val requestProcessor: SubmissionRequestProcessor,
    private val requestReleaser: SubmissionRequestReleaser,
    private val requestCleaner: SubmissionRequestCleaner,
) {
    fun createRequest(rqt: ExtSubmitRequest): Pair<String, Int> {
        val withTabFiles = pageTabService.generatePageTab(rqt.submission)
        val submission = withTabFiles.copy(version = persistenceService.getNextVersion(rqt.submission.accNo))
        val request = SubmissionRequest(
            submission,
            rqt.draftKey,
            rqt.notifyTo,
            status = REQUESTED,
            totalFiles = 0,
            currentIndex = 0,
            modificationTime = OffsetDateTime.now(),
        )

        return requestService.createSubmissionRequest(request)
    }

    fun indexRequest(accNo: String, version: Int): Unit = requestIndexer.indexRequest(accNo, version)

    fun loadRequest(accNo: String, version: Int): Unit = requestLoader.loadRequest(accNo, version)

    fun cleanRequest(accNo: String, version: Int): Unit = requestCleaner.cleanCurrentVersion(accNo, version)

    fun processRequest(accNo: String, version: Int): ExtSubmission = requestProcessor.processRequest(accNo, version)

    fun checkReleased(accNo: String, version: Int): ExtSubmission = requestReleaser.checkReleased(accNo, version)

    fun release(accNo: String) = requestReleaser.releaseSubmission(accNo)

    fun handleRequest(accNo: String, version: Int): ExtSubmission {
        return when (requestService.getRequestStatus(accNo, version)) {
            REQUESTED -> completeRequest(accNo, version)
            INDEXED -> loadRequestFiles(accNo, version)
            LOADED -> cleanRequestFiles(accNo, version)
            CLEANED -> processRequestFiles(accNo, version)
            FILES_COPIED -> checkReleased(accNo, version)
            else -> throw IllegalStateException("Request accNo=$accNo, version='$version' has been already processed")
        }
    }

    private fun completeRequest(accNo: String, version: Int): ExtSubmission {
        indexRequest(accNo, version)
        loadRequest(accNo, version)
        cleanRequest(accNo, version)
        processRequest(accNo, version)
        return checkReleased(accNo, version)
    }

    private fun loadRequestFiles(accNo: String, version: Int): ExtSubmission {
        loadRequest(accNo, version)
        cleanRequest(accNo, version)
        processRequest(accNo, version)
        return checkReleased(accNo, version)
    }

    private fun cleanRequestFiles(accNo: String, version: Int): ExtSubmission {
        cleanRequest(accNo, version)
        processRequest(accNo, version)
        return checkReleased(accNo, version)
    }

    private fun processRequestFiles(accNo: String, version: Int): ExtSubmission {
        processRequest(accNo, version)
        return checkReleased(accNo, version)
    }
}
