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
import ac.uk.ebi.biostd.submission.domain.submission.SubmissionService.Companion.SYNC_SUBMIT_TIMEOUT
import ebi.ac.uk.coroutines.waitUntil
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
import mu.KotlinLogging
import java.time.Duration.ofMinutes

private val logger = KotlinLogging.logger {}

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
    ) {
        requestSaver.saveRequest(accNo, version, properties.processId)
    }

    override suspend fun finalizeRequest(
        accNo: String,
        version: Int,
    ) {
        requestCleaner.finalizeRequest(accNo, version, properties.processId)
    }

    override suspend fun handleRequest(
        accNo: String,
        version: Int,
    ): ExtSubmission {
        when (requestService.getRequestStatus(accNo, version)) {
            REQUESTED -> indexRequest(accNo, version)
            INDEXED -> loadRequest(accNo, version)
            LOADED -> indexToCleanRequest(accNo, version)
            INDEXED_CLEANED -> cleanRequest(accNo, version)
            CLEANED -> processRequest(accNo, version)
            FILES_COPIED -> checkReleased(accNo, version)
            CHECK_RELEASED -> saveRequest(accNo, version)
            PERSISTED -> finalizeRequest(accNo, version)
            PROCESSED -> logger.info { "Submission $accNo, $version has been already proccessed." }
        }

        waitUntil(timeout = ofMinutes(SYNC_SUBMIT_TIMEOUT)) { requestService.isRequestCompleted(accNo, version) }
        return submissionQueryService.getExtendedSubmission(accNo)
    }
}
