package ac.uk.ebi.biostd.submission.domain.request

import ac.uk.ebi.biostd.common.properties.SecurityProperties
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ebi.ac.uk.base.orFalse
import ebi.ac.uk.model.RequestStatus
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
            when {
                properties.preventFileDeletion -> validateRequest(it)
                else -> it.withNewStatus(VALIDATED)
            }
        }

    private suspend fun validateRequest(rqt: SubmissionRequest): SubmissionRequest {
        val submission = rqt.process!!.submission
        val accNo = submission.accNo
        val submitter = submission.submitter
        val currentReleased = queryService.findCoreInfo(accNo)?.released.orFalse()

        if (currentReleased && rqt.hasFilesChanges && userPrivilegesService.canDeleteFiles(submitter, accNo).not()) {
            logger.error { "$accNo ${submission.owner} The user $submitter is not allowed to modify/delete files" }
            return rqt.withErrors(listOf("File deletion/modifications require admin permission."))
        }

        return rqt.withNewStatus(VALIDATED)
    }

    /**
     * A submission request has file changes if:
     * - Any file has been deleted (deprecatedFiles > 0)
     * - Any file has been replaced (conflictingFiles > 0)
     */
    private val SubmissionRequest.hasFilesChanges: Boolean
        get() = process!!.fileChanges.deprecatedFiles > 0 || process!!.fileChanges.conflictingFiles > 0
}
