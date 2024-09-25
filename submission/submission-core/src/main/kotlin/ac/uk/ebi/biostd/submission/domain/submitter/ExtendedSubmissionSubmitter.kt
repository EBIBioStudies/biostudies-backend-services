package ac.uk.ebi.biostd.submission.domain.submitter

import ac.uk.ebi.biostd.common.properties.SubmissionTaskProperties
import ac.uk.ebi.biostd.persistence.common.request.ExtSubmitRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
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
import java.time.Duration.ofMinutes

@Suppress("TooManyFunctions")
class ExtendedSubmissionSubmitter(
    private val localExtSubmissionSubmitter: LocalExtSubmissionSubmitter,
    private val remoteExtSubmissionSubmitter: RemoteExtSubmissionSubmitter,
    private val submissionTaskProperties: SubmissionTaskProperties,
    private val requestService: SubmissionRequestPersistenceService,
    private val queryService: SubmissionPersistenceQueryService,
) : ExtSubmissionSubmitter {
    override suspend fun createRequest(rqt: ExtSubmitRequest): Pair<String, Int> {
        return localExtSubmissionSubmitter.createRequest(rqt)
    }

    override suspend fun indexRequest(
        accNo: String,
        version: Int,
    ) {
        when (submissionTaskProperties.enabled) {
            true -> remoteExtSubmissionSubmitter.indexRequest(accNo, version)
            else -> localExtSubmissionSubmitter.indexRequest(accNo, version)
        }
    }

    override suspend fun loadRequest(
        accNo: String,
        version: Int,
    ) {
        when (submissionTaskProperties.enabled) {
            true -> remoteExtSubmissionSubmitter.loadRequest(accNo, version)
            else -> localExtSubmissionSubmitter.loadRequest(accNo, version)
        }
    }

    override suspend fun indexToCleanRequest(
        accNo: String,
        version: Int,
    ) {
        when (submissionTaskProperties.enabled) {
            true -> remoteExtSubmissionSubmitter.indexToCleanRequest(accNo, version)
            else -> localExtSubmissionSubmitter.indexToCleanRequest(accNo, version)
        }
    }

    override suspend fun validateRequest(
        accNo: String,
        version: Int,
    ) {
        when (submissionTaskProperties.enabled) {
            true -> remoteExtSubmissionSubmitter.validateRequest(accNo, version)
            else -> localExtSubmissionSubmitter.validateRequest(accNo, version)
        }
    }

    override suspend fun cleanRequest(
        accNo: String,
        version: Int,
    ) {
        when (submissionTaskProperties.enabled) {
            true -> remoteExtSubmissionSubmitter.cleanRequest(accNo, version)
            else -> localExtSubmissionSubmitter.cleanRequest(accNo, version)
        }
    }

    override suspend fun processRequest(
        accNo: String,
        version: Int,
    ) {
        when (submissionTaskProperties.enabled) {
            true -> remoteExtSubmissionSubmitter.processRequest(accNo, version)
            else -> localExtSubmissionSubmitter.processRequest(accNo, version)
        }
    }

    override suspend fun checkReleased(
        accNo: String,
        version: Int,
    ) {
        when (submissionTaskProperties.enabled) {
            true -> remoteExtSubmissionSubmitter.checkReleased(accNo, version)
            else -> localExtSubmissionSubmitter.checkReleased(accNo, version)
        }
    }

    override suspend fun saveRequest(
        accNo: String,
        version: Int,
    ) {
        when (submissionTaskProperties.enabled) {
            true -> remoteExtSubmissionSubmitter.saveRequest(accNo, version)
            else -> localExtSubmissionSubmitter.saveRequest(accNo, version)
        }
    }

    override suspend fun finalizeRequest(
        accNo: String,
        version: Int,
    ) {
        when (submissionTaskProperties.enabled) {
            true -> remoteExtSubmissionSubmitter.finalizeRequest(accNo, version)
            else -> localExtSubmissionSubmitter.finalizeRequest(accNo, version)
        }
    }

    override suspend fun handleRequest(
        accNo: String,
        version: Int,
    ): ExtSubmission =
        when (submissionTaskProperties.enabled) {
            true -> handleRemoteRequest(accNo, version)
            else -> localExtSubmissionSubmitter.handleRequest(accNo, version)
        }

    private suspend fun handleRemoteRequest(
        accNo: String,
        version: Int,
    ): ExtSubmission =
        when (requestService.getRequestStatus(accNo, version)) {
            REQUESTED -> triggerAndWait(accNo, version) { indexRequest(accNo, version) }
            INDEXED -> triggerAndWait(accNo, version) { loadRequest(accNo, version) }
            LOADED -> triggerAndWait(accNo, version) { indexToCleanRequest(accNo, version) }
            INDEXED_CLEANED -> triggerAndWait(accNo, version) { validateRequest(accNo, version) }
            VALIDATED -> triggerAndWait(accNo, version) { cleanRequest(accNo, version) }
            CLEANED -> triggerAndWait(accNo, version) { processRequest(accNo, version) }
            FILES_COPIED -> triggerAndWait(accNo, version) { checkReleased(accNo, version) }
            CHECK_RELEASED -> triggerAndWait(accNo, version) { localExtSubmissionSubmitter.saveRequest(accNo, version) }
            PERSISTED -> triggerAndWait(accNo, version) { finalizeRequest(accNo, version) }
            INVALID -> error("Request accNo=$accNo, version=$version is in an invalid state")
            PROCESSED -> error("Request accNo=$accNo, version=$version has been already processed")
        }

    private suspend fun triggerAndWait(
        accNo: String,
        version: Int,
        stageTrigger: suspend () -> Unit,
    ): ExtSubmission {
        stageTrigger()
        waitUntil(timeout = ofMinutes(SYNC_SUBMIT_TIMEOUT)) { queryService.existByAccNoAndVersion(accNo, version) }
        return queryService.getExtByAccNo(accNo)
    }
}
