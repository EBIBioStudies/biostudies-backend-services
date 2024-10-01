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
import ac.uk.ebi.biostd.submission.domain.request.SubmissionRequestValidator
import ac.uk.ebi.biostd.submission.domain.submission.SubmissionService.Companion.SYNC_SUBMIT_TIMEOUT
import ebi.ac.uk.coroutines.waitUntil
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.model.RequestStatus.CHECK_RELEASED
import ebi.ac.uk.model.RequestStatus.CLEANED
import ebi.ac.uk.model.RequestStatus.FILES_COPIED
import ebi.ac.uk.model.RequestStatus.INDEXED
import ebi.ac.uk.model.RequestStatus.INDEXED_CLEANED
import ebi.ac.uk.model.RequestStatus.INVALID
import ebi.ac.uk.model.RequestStatus.LOADED
import ebi.ac.uk.model.RequestStatus.PERSISTED
import ebi.ac.uk.model.RequestStatus.PROCESSED
import ebi.ac.uk.model.RequestStatus.REQUESTED
import ebi.ac.uk.model.RequestStatus.VALIDATED
import mu.KotlinLogging
import uk.ac.ebi.events.service.EventsPublisherService
import java.time.Duration.ofMinutes

private val logger = KotlinLogging.logger {}

@Suppress("LongParameterList", "TooManyFunctions")
class LocalExtSubmissionSubmitter(
    private val properties: ApplicationProperties,
    private val pageTabService: PageTabService,
    private val requestService: SubmissionRequestPersistenceService,
    private val persistenceService: SubmissionPersistenceService,
    private val requestIndexer: SubmissionRequestIndexer,
    private val requestLoader: SubmissionRequestLoader,
    private val requestToCleanIndexer: SubmissionRequestCleanIndexer,
    private val requestValidator: SubmissionRequestValidator,
    private val requestProcessor: SubmissionRequestProcessor,
    private val requestReleaser: SubmissionRequestReleaser,
    private val requestCleaner: SubmissionRequestCleaner,
    private val requestSaver: SubmissionRequestSaver,
    private val submissionQueryService: ExtSubmissionQueryService,
    private val eventsPublisherService: EventsPublisherService,
) : ExtSubmissionSubmitter {
    override suspend fun createRqt(rqt: ExtSubmitRequest): Pair<String, Int> {
        val withTabFiles = pageTabService.generatePageTab(rqt.submission)
        val submission = withTabFiles.copy(version = persistenceService.getNextVersion(rqt.submission.accNo))
        val request =
            SubmissionRequest(
                submission = submission,
                notifyTo = rqt.notifyTo,
                draftKey = rqt.draftKey,
                silentMode = rqt.silentMode,
            )
        return requestService.createRequest(request)
    }

    @Suppress("CyclomaticComplexMethod")
    override suspend fun completeRqt(
        accNo: String,
        version: Int,
    ) {
        suspend fun fromSavedSubmission() {
            requestCleaner.finalizeRequest(accNo, version, properties.processId)
        }

        suspend fun fromCheckReleased() {
            val rqt = requestSaver.saveRequest(accNo, version, properties.processId)
            if (rqt.silentMode.not()) eventsPublisherService.submissionSubmitted(accNo, rqt.notifyTo)
            fromSavedSubmission()
        }

        suspend fun fromFilesCopied() {
            requestReleaser.checkReleased(accNo, version, properties.processId)
            fromCheckReleased()
        }

        suspend fun fromCleaned() {
            requestProcessor.processRequest(accNo, version, properties.processId)
            fromFilesCopied()
        }

        suspend fun fromValidated() {
            requestCleaner.cleanCurrentVersion(accNo, version, properties.processId)
            fromCleaned()
        }

        suspend fun fromIndexedToClean() {
            val rqt = requestValidator.validateRequest(accNo, version, properties.processId)
            if (rqt.status == VALIDATED) fromValidated()
        }

        suspend fun fromLoaded() {
            requestToCleanIndexer.indexToCleanRequest(accNo, version, properties.processId)
            fromIndexedToClean()
        }

        suspend fun fromIndexed() {
            requestLoader.loadRequest(accNo, version, properties.processId)
            fromLoaded()
        }

        suspend fun fromRequested() {
            requestIndexer.indexRequest(accNo, version, properties.processId)
            fromIndexed()
        }

        val status = requestService.getRequestStatus(accNo, version)
        when (status) {
            REQUESTED -> fromRequested()
            INDEXED -> fromIndexed()
            LOADED -> fromLoaded()
            INDEXED_CLEANED -> fromIndexedToClean()
            VALIDATED -> fromValidated()
            CLEANED -> fromCleaned()
            FILES_COPIED -> fromFilesCopied()
            CHECK_RELEASED -> fromCheckReleased()
            PERSISTED -> fromSavedSubmission()
            PROCESSED -> logger.info { "Submission, $accNo, $version has been already processed." }
            INVALID -> logger.info { "Submission, $accNo, $version is in INVALID. Errors need to be fixed." }
        }
    }

    override suspend fun indexRequest(
        accNo: String,
        version: Int,
    ) {
        requestIndexer.indexRequest(accNo, version, properties.processId)
        eventsPublisherService.requestIndexed(accNo, version)
    }

    override suspend fun loadRequest(
        accNo: String,
        version: Int,
    ) {
        requestLoader.loadRequest(accNo, version, properties.processId)
        eventsPublisherService.requestLoaded(accNo, version)
    }

    override suspend fun indexToCleanRequest(
        accNo: String,
        version: Int,
    ) {
        requestToCleanIndexer.indexToCleanRequest(accNo, version, properties.processId)
        eventsPublisherService.requestIndexedToClean(accNo, version)
    }

    override suspend fun validateRequest(
        accNo: String,
        version: Int,
    ) {
        val request = requestValidator.validateRequest(accNo, version, properties.processId)
        if (request.status == VALIDATED) eventsPublisherService.requestValidated(accNo, version)
    }

    override suspend fun cleanRequest(
        accNo: String,
        version: Int,
    ) {
        requestCleaner.cleanCurrentVersion(accNo, version, properties.processId)
        eventsPublisherService.requestCleaned(accNo, version)
    }

    override suspend fun processRequest(
        accNo: String,
        version: Int,
    ) {
        requestProcessor.processRequest(accNo, version, properties.processId)
        eventsPublisherService.requestFilesCopied(accNo, version)
    }

    override suspend fun checkReleased(
        accNo: String,
        version: Int,
    ) {
        requestReleaser.checkReleased(accNo, version, properties.processId)
        eventsPublisherService.requestCheckedRelease(accNo, version)
    }

    override suspend fun saveRequest(
        accNo: String,
        version: Int,
    ) {
        val rqt = requestSaver.saveRequest(accNo, version, properties.processId)
        if (rqt.silentMode.not()) eventsPublisherService.submissionSubmitted(accNo, rqt.notifyTo)
        eventsPublisherService.submissionPersisted(accNo, version)
    }

    override suspend fun finalizeRequest(
        accNo: String,
        version: Int,
    ) {
        requestCleaner.finalizeRequest(accNo, version, properties.processId)
        eventsPublisherService.submissionFinalized(accNo, version)
    }

    override suspend fun handleRequest(
        accNo: String,
        version: Int,
    ): ExtSubmission {
        when (requestService.getRequestStatus(accNo, version)) {
            REQUESTED -> indexRequest(accNo, version)
            INDEXED -> loadRequest(accNo, version)
            LOADED -> indexToCleanRequest(accNo, version)
            INDEXED_CLEANED -> validateRequest(accNo, version)
            VALIDATED -> cleanRequest(accNo, version)
            CLEANED -> processRequest(accNo, version)
            FILES_COPIED -> checkReleased(accNo, version)
            CHECK_RELEASED -> saveRequest(accNo, version)
            PERSISTED -> finalizeRequest(accNo, version)
            INVALID -> logger.info { "Submission $accNo, $version is in an invalid state" }
            PROCESSED -> logger.info { "Submission $accNo, $version has been already processed." }
        }

        waitUntil(timeout = ofMinutes(SYNC_SUBMIT_TIMEOUT)) { requestService.isRequestCompleted(accNo, version) }
        return submissionQueryService.getExtendedSubmission(accNo)
    }
}
