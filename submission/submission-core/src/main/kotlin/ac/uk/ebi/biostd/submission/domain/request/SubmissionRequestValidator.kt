package ac.uk.ebi.biostd.submission.domain.request

import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.service.RqtUpdate
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ebi.ac.uk.base.orFalse
import ebi.ac.uk.model.RequestStatus
import ebi.ac.uk.model.RequestStatus.INVALID
import ebi.ac.uk.model.RequestStatus.VALIDATED
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import mu.KotlinLogging
import uk.ac.ebi.events.service.EventsPublisherService

private val logger = KotlinLogging.logger {}

class SubmissionRequestValidator(
    private val userPrivilegesService: IUserPrivilegesService,
    private val eventsPublisherService: EventsPublisherService,
    private val queryService: SubmissionPersistenceQueryService,
    private val requestService: SubmissionRequestPersistenceService,
) {
    suspend fun validateRequest(
        accNo: String,
        version: Int,
        processId: String,
    ) {
        var requestStatus = INVALID

        requestService.onRequest(accNo, version, RequestStatus.INDEXED_CLEANED, processId) {
            requestStatus = validateRequest(it)
            RqtUpdate(it.withNewStatus(requestStatus))
        }

        if (requestStatus == VALIDATED) eventsPublisherService.requestValidated(accNo, version)
    }

    internal suspend fun validateRequest(rqt: SubmissionRequest): RequestStatus {
        return validateFilesChanges(rqt)
    }

    private suspend fun validateFilesChanges(rqt: SubmissionRequest): RequestStatus {
        val accNo = rqt.submission.accNo
        val submitter = rqt.submission.submitter
        val currentReleased = queryService.findCoreInfo(rqt.submission.accNo)?.released.orFalse()

        if (currentReleased &&
            rqt.hasFilesChanges &&
            userPrivilegesService.canUpdatePublicSubmission(submitter, accNo).not()
        ) {
            logger.error { "$accNo ${rqt.submission.owner} The user $submitter is not allowed to modify files" }
            return INVALID
        }

        return VALIDATED
    }

    /**
     * A submission request has file changes if:
     * - Any file has been deleted (deprecatedFiles > 0)
     * - Any file has been replaced (conflictingFiles > 2). 2 is used as a base since, for resubmissions, the two
     *   pagetab files will be always regenerated
     */
    private val SubmissionRequest.hasFilesChanges: Boolean
        get() = deprecatedFiles > 0 || conflictingFiles > 2
}
