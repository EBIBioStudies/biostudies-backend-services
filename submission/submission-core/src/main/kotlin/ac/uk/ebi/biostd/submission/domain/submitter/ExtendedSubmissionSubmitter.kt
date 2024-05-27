package ac.uk.ebi.biostd.submission.domain.submitter

import ac.uk.ebi.biostd.common.properties.SubmissionTaskProperties
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.CHECK_RELEASED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.CLEANED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.FILES_COPIED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.INDEXED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.INDEXED_CLEANED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.LOADED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.PERSISTED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.PROCESSED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.REQUESTED
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.submission.domain.submission.SubmissionService.Companion.SYNC_SUBMIT_TIMEOUT
import ebi.ac.uk.coroutines.waitUntil
import ebi.ac.uk.extended.model.ExtSubmission
import java.time.Duration.ofMinutes

@Suppress("TooManyFunctions")
class ExtendedSubmissionSubmitter(
    private val localExtSubmissionSubmitter: LocalExtSubmissionSubmitter,
    private val remoteExtSubmissionSubmitter: RemoteExtSubmissionSubmitter,
    private val submissionTaskProperties: SubmissionTaskProperties,
    private val requestService: SubmissionRequestPersistenceService,
    private val queryService: SubmissionPersistenceQueryService,
) : ExtSubmissionSubmitter by localExtSubmissionSubmitter {
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

    override suspend fun handleRequest(
        accNo: String,
        version: Int,
    ): ExtSubmission {
        return when (submissionTaskProperties.enabled) {
            true -> handleRemoteRequest(accNo, version)
            else -> localExtSubmissionSubmitter.handleRequest(accNo, version)
        }
    }

    private suspend fun handleRemoteRequest(
        accNo: String,
        version: Int,
    ): ExtSubmission {
        return when (requestService.getRequestStatus(accNo, version)) {
            REQUESTED -> triggerAndWait(accNo, version) { indexRequest(accNo, version) }
            INDEXED -> triggerAndWait(accNo, version) { loadRequest(accNo, version) }
            LOADED -> triggerAndWait(accNo, version) { indexToCleanRequest(accNo, version) }
            INDEXED_CLEANED -> triggerAndWait(accNo, version) { cleanRequest(accNo, version) }
            CLEANED -> triggerAndWait(accNo, version) { processRequest(accNo, version) }
            FILES_COPIED -> triggerAndWait(accNo, version) { checkReleased(accNo, version) }
            CHECK_RELEASED -> localExtSubmissionSubmitter.saveAndFinalize(accNo, version)
            PERSISTED -> finalizeRequest(accNo, version)
            PROCESSED -> error("Request accNo=$accNo, version=$version has been already processed")
        }
    }

    private suspend fun triggerAndWait(
        accNo: String,
        version: Int,
        stageTrigger: suspend () -> Unit,
    ): ExtSubmission {
        stageTrigger()
        return waitUntil(
            ofMinutes(SYNC_SUBMIT_TIMEOUT),
            conditionEvaluator = { queryService.existByAccNoAndVersion(accNo, version) },
            processFunction = { queryService.getExtByAccNo(accNo) },
        )
    }
}
