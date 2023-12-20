package ac.uk.ebi.biostd.submission.domain.submitter

import ac.uk.ebi.biostd.common.properties.SubmissionTaskProperties
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.CHECK_RELEASED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.CLEANED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.FILES_COPIED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.INDEXED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.LOADED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.PERSISTED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.REQUESTED
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ebi.ac.uk.extended.model.ExtSubmission

@Suppress("TooManyFunctions")
class ExtendedSubmissionSubmitter(
    private val localExtSubmissionSubmitter: LocalExtSubmissionSubmitter,
    private val remoteExtSubmissionSubmitter: RemoteExtSubmissionSubmitter,
    private val submissionTaskProperties: SubmissionTaskProperties,
    private val requestService: SubmissionRequestPersistenceService,
) : ExtSubmissionSubmitter by localExtSubmissionSubmitter {
    override suspend fun loadRequest(accNo: String, version: Int) {
        when (submissionTaskProperties.enabled) {
            true -> remoteExtSubmissionSubmitter.loadRequest(accNo, version)
            else -> localExtSubmissionSubmitter.loadRequest(accNo, version)
        }
    }

    override suspend fun cleanRequest(accNo: String, version: Int) {
        when (submissionTaskProperties.enabled) {
            true -> remoteExtSubmissionSubmitter.cleanRequest(accNo, version)
            else -> localExtSubmissionSubmitter.cleanRequest(accNo, version)
        }
    }

    override suspend fun processRequest(accNo: String, version: Int) {
        when (submissionTaskProperties.enabled) {
            true -> remoteExtSubmissionSubmitter.processRequest(accNo, version)
            else -> localExtSubmissionSubmitter.processRequest(accNo, version)
        }
    }

    override suspend fun checkReleased(accNo: String, version: Int) {
        when (submissionTaskProperties.enabled) {
            true -> remoteExtSubmissionSubmitter.checkReleased(accNo, version)
            else -> localExtSubmissionSubmitter.checkReleased(accNo, version)
        }
    }

    override suspend fun handleRequest(accNo: String, version: Int): ExtSubmission {
        return when (requestService.getRequestStatus(accNo, version)) {
            REQUESTED -> completeRequest(accNo, version)
            INDEXED -> loadRequestFiles(accNo, version)
            LOADED -> cleanRequestFiles(accNo, version)
            CLEANED -> processRequestFiles(accNo, version)
            FILES_COPIED -> releaseSubmission(accNo, version)
            CHECK_RELEASED -> saveAndFinalize(accNo, version)
            PERSISTED -> finalizeRequest(accNo, version)
            else -> throw IllegalStateException("Request accNo=$accNo, version=$version has been already processed")
        }
    }

    private suspend fun completeRequest(accNo: String, version: Int): ExtSubmission {
        indexRequest(accNo, version)
        loadRequest(accNo, version)
        cleanRequest(accNo, version)
        processRequest(accNo, version)
        checkReleased(accNo, version)
        saveRequest(accNo, version)
        return finalizeRequest(accNo, version)
    }

    private suspend fun loadRequestFiles(accNo: String, version: Int): ExtSubmission {
        loadRequest(accNo, version)
        cleanRequest(accNo, version)
        processRequest(accNo, version)
        checkReleased(accNo, version)
        saveRequest(accNo, version)
        return finalizeRequest(accNo, version)
    }

    private suspend fun cleanRequestFiles(accNo: String, version: Int): ExtSubmission {
        cleanRequest(accNo, version)
        processRequest(accNo, version)
        checkReleased(accNo, version)
        saveRequest(accNo, version)
        return finalizeRequest(accNo, version)
    }

    private suspend fun processRequestFiles(accNo: String, version: Int): ExtSubmission {
        processRequest(accNo, version)
        checkReleased(accNo, version)
        saveRequest(accNo, version)
        return finalizeRequest(accNo, version)
    }

    private suspend fun releaseSubmission(accNo: String, version: Int): ExtSubmission {
        checkReleased(accNo, version)
        saveRequest(accNo, version)
        return finalizeRequest(accNo, version)
    }

    private suspend fun saveAndFinalize(accNo: String, version: Int): ExtSubmission {
        saveRequest(accNo, version)
        return finalizeRequest(accNo, version)
    }
}
