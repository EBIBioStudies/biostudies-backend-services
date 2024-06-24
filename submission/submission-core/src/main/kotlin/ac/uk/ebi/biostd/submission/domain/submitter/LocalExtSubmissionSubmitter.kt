package ac.uk.ebi.biostd.submission.domain.submitter

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.request.ExtSubmitRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.pagetab.PageTabService
import ac.uk.ebi.biostd.submission.domain.extended.ExtSubmissionQueryService
import ac.uk.ebi.biostd.submission.domain.request.SubmissionRequestCleanIndexer
import ac.uk.ebi.biostd.submission.domain.request.SubmissionRequestCleaner
import ac.uk.ebi.biostd.submission.domain.request.SubmissionRequestIndexer
import ac.uk.ebi.biostd.submission.domain.request.SubmissionRequestLoader
import ac.uk.ebi.biostd.submission.domain.request.SubmissionRequestProcessor
import ac.uk.ebi.biostd.submission.domain.request.SubmissionRequestReleaser
import ac.uk.ebi.biostd.submission.domain.request.SubmissionRequestSaver
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.model.RequestStatus.CHECK_RELEASED
import ebi.ac.uk.model.RequestStatus.CLEANED
import ebi.ac.uk.model.RequestStatus.FILES_COPIED
import ebi.ac.uk.model.RequestStatus.INDEXED
import ebi.ac.uk.model.RequestStatus.INDEXED_CLEANED
import ebi.ac.uk.model.RequestStatus.LOADED
import ebi.ac.uk.model.RequestStatus.PERSISTED
import ebi.ac.uk.model.RequestStatus.PROCESSED
import ebi.ac.uk.model.RequestStatus.REQUESTED

@Suppress("LongParameterList", "TooManyFunctions")
class LocalExtSubmissionSubmitter(
    private val properties: ApplicationProperties,
    private val pageTabService: PageTabService,
    private val requestService: SubmissionRequestPersistenceService,
    private val persistenceService: SubmissionPersistenceService,
    private val requestIndexer: SubmissionRequestIndexer,
    private val requestToCleanIndexer: SubmissionRequestCleanIndexer,
    private val requestLoader: SubmissionRequestLoader,
    private val requestProcessor: SubmissionRequestProcessor,
    private val requestReleaser: SubmissionRequestReleaser,
    private val requestCleaner: SubmissionRequestCleaner,
    private val requestSaver: SubmissionRequestSaver,
    private val submissionQueryService: ExtSubmissionQueryService,
) : ExtSubmissionSubmitter {
    override suspend fun createRequest(rqt: ExtSubmitRequest): Pair<String, Int> {
        val withTabFiles = pageTabService.generatePageTab(rqt.submission)
        val submission = withTabFiles.copy(version = persistenceService.getNextVersion(rqt.submission.accNo))
        val request = SubmissionRequest(submission = submission, notifyTo = rqt.notifyTo, draftKey = rqt.draftKey)
        return requestService.createRequest(request)
    }

    override suspend fun indexRequest(
        accNo: String,
        version: Int,
    ) {
        requestIndexer.indexRequest(accNo, version, properties.processId)
    }

    override suspend fun loadRequest(
        accNo: String,
        version: Int,
    ) {
        requestLoader.loadRequest(accNo, version, properties.processId)
    }

    override suspend fun cleanRequest(
        accNo: String,
        version: Int,
    ) {
        requestCleaner.cleanCurrentVersion(accNo, version, properties.processId)
    }

    override suspend fun indexToCleanRequest(
        accNo: String,
        version: Int,
    ) {
        requestToCleanIndexer.indexRequest(accNo, version, properties.processId)
    }

    override suspend fun processRequest(
        accNo: String,
        version: Int,
    ) {
        requestProcessor.processRequest(accNo, version, properties.processId)
    }

    override suspend fun checkReleased(
        accNo: String,
        version: Int,
    ) {
        requestReleaser.checkReleased(accNo, version, properties.processId)
    }

    override suspend fun saveRequest(
        accNo: String,
        version: Int,
    ): ExtSubmission {
        return requestSaver.saveRequest(accNo, version, properties.processId)
    }

    override suspend fun finalizeRequest(
        accNo: String,
        version: Int,
    ): ExtSubmission {
        requestCleaner.finalizeRequest(accNo, version, properties.processId)
        return submissionQueryService.getExtendedSubmission(accNo, includeFileListFiles = false)
    }

    override suspend fun handleRequest(
        accNo: String,
        version: Int,
    ): ExtSubmission {
        return when (requestService.getRequestStatus(accNo, version)) {
            REQUESTED -> completeRequest(accNo, version)
            INDEXED -> loadRequestFiles(accNo, version)
            LOADED -> indexToCleanFiles(accNo, version)
            INDEXED_CLEANED -> cleanRequestFiles(accNo, version)
            CLEANED -> processRequestFiles(accNo, version)
            FILES_COPIED -> releaseSubmission(accNo, version)
            CHECK_RELEASED -> saveAndFinalize(accNo, version)
            PERSISTED -> finalizeRequest(accNo, version)
            PROCESSED -> submissionQueryService.getExtendedSubmission(accNo, includeFileListFiles = false)
        }
    }

    private suspend fun completeRequest(
        accNo: String,
        version: Int,
    ): ExtSubmission {
        indexRequest(accNo, version)
        loadRequest(accNo, version)
        indexToCleanRequest(accNo, version)
        cleanRequest(accNo, version)
        processRequest(accNo, version)
        checkReleased(accNo, version)
        saveRequest(accNo, version)
        return finalizeRequest(accNo, version)
    }

    private suspend fun loadRequestFiles(
        accNo: String,
        version: Int,
    ): ExtSubmission {
        loadRequest(accNo, version)
        indexToCleanFiles(accNo, version)
        cleanRequest(accNo, version)
        processRequest(accNo, version)
        checkReleased(accNo, version)
        saveRequest(accNo, version)
        return finalizeRequest(accNo, version)
    }

    private suspend fun indexToCleanFiles(
        accNo: String,
        version: Int,
    ): ExtSubmission {
        indexToCleanRequest(accNo, version)
        cleanRequest(accNo, version)
        processRequest(accNo, version)
        checkReleased(accNo, version)
        saveRequest(accNo, version)
        return finalizeRequest(accNo, version)
    }

    private suspend fun cleanRequestFiles(
        accNo: String,
        version: Int,
    ): ExtSubmission {
        cleanRequest(accNo, version)
        processRequest(accNo, version)
        checkReleased(accNo, version)
        saveRequest(accNo, version)
        return finalizeRequest(accNo, version)
    }

    private suspend fun processRequestFiles(
        accNo: String,
        version: Int,
    ): ExtSubmission {
        processRequest(accNo, version)
        checkReleased(accNo, version)
        saveRequest(accNo, version)
        return finalizeRequest(accNo, version)
    }

    private suspend fun releaseSubmission(
        accNo: String,
        version: Int,
    ): ExtSubmission {
        checkReleased(accNo, version)
        saveRequest(accNo, version)
        return finalizeRequest(accNo, version)
    }

    internal suspend fun saveAndFinalize(
        accNo: String,
        version: Int,
    ): ExtSubmission {
        saveRequest(accNo, version)
        return finalizeRequest(accNo, version)
    }
}
