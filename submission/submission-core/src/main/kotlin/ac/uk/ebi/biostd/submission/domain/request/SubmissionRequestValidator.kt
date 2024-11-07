package ac.uk.ebi.biostd.submission.domain.request

import ac.uk.ebi.biostd.common.properties.SecurityProperties
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ebi.ac.uk.base.orFalse
import ebi.ac.uk.model.RequestStatus
import ebi.ac.uk.model.RequestStatus.INVALID
import ebi.ac.uk.model.RequestStatus.VALIDATED
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class SubmissionRequestValidator(
    private val userPrivilegesService: IUserPrivilegesService,
    private val queryService: SubmissionPersistenceQueryService,
    private val requestService: SubmissionRequestPersistenceService,
    private val properties: SecurityProperties,
) {
    suspend fun validateRequest(
        accNo: String,
        version: Int,
        processId: String,
    ): SubmissionRequest =
        requestService.onRequest(accNo, version, RequestStatus.INDEXED_CLEANED, processId) {
            val requestStatus = if (properties.preventFileDeletion) validateRequest(it) else VALIDATED
            it.withNewStatus(requestStatus)
        }

    private suspend fun validateRequest(rqt: SubmissionRequest): RequestStatus {
        val accNo = rqt.process.submission.accNo
        val submitter = rqt.process.submission.submitter
        val currentReleased = queryService.findCoreInfo(rqt.process.submission.accNo)?.released.orFalse()

        if (currentReleased && rqt.hasFilesChanges && userPrivilegesService.canDeleteFiles(submitter, accNo).not()) {
            logger.error { "$accNo ${rqt.process.submission.owner} The user $submitter is not allowed to modify files" }
            return INVALID
        }

        return VALIDATED
    }

    /**
     * A submission request has file changes if:
     * - Any file has been deleted (deprecatedFiles > 0)
     * - Any file has been replaced (conflictingFiles > 0)
     */
    private val SubmissionRequest.hasFilesChanges: Boolean
        get() = process.fileChanges.deprecatedFiles > 0 || process.fileChanges.conflictingFiles > 0
}
