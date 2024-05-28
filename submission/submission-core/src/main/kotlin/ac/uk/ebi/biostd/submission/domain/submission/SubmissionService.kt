package ac.uk.ebi.biostd.submission.domain.submission

import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.api.FileStorageService
import ac.uk.ebi.biostd.submission.exceptions.UserCanNotDeleteSubmission
import ac.uk.ebi.biostd.submission.exceptions.UserCanNotDeleteSubmissions
import ac.uk.ebi.biostd.submission.model.AcceptedSubmission
import ac.uk.ebi.biostd.submission.model.SubmitRequest
import ebi.ac.uk.coroutines.waitUntil
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import ebi.ac.uk.security.integration.model.api.SecurityUser
import ebi.ac.uk.util.collections.ifNotEmpty
import mu.KotlinLogging
import uk.ac.ebi.events.service.EventsPublisherService
import java.time.Duration.ofMinutes

private val logger = KotlinLogging.logger {}

@Suppress("LongParameterList")
class SubmissionService(
    private val queryService: SubmissionPersistenceQueryService,
    private val userPrivilegesService: IUserPrivilegesService,
    private val submissionSubmitter: SubmissionSubmitter,
    private val eventsPublisherService: EventsPublisherService,
    private val submissionPersistenceService: SubmissionPersistenceService,
    private val fileStorageService: FileStorageService,
    private val requestQueryService: SubmissionRequestPersistenceService,
) {
    suspend fun submit(rqt: SubmitRequest): ExtSubmission {
        logger.info { "${rqt.accNo} ${rqt.owner} Received sync submit request with draft key '${rqt.draftKey}'" }

        val (accNo, version) = submissionSubmitter.createRequest(rqt)
        eventsPublisherService.requestCreated(accNo, version)

        return waitUntil(
            ofMinutes(SYNC_SUBMIT_TIMEOUT),
            conditionEvaluator = { requestQueryService.isRequestCompleted(accNo, version) },
            processFunction = { queryService.getExtByAccNo(accNo) },
        )
    }

    suspend fun submitAsync(rqt: SubmitRequest): AcceptedSubmission {
        logger.info { "${rqt.accNo} ${rqt.owner} Received async submit request with draft key '${rqt.draftKey}'" }
        val (accNo, version) = submissionSubmitter.createRequest(rqt)
        eventsPublisherService.requestCreated(accNo, version)

        return AcceptedSubmission(accNo, version)
    }

    suspend fun deleteSubmission(
        accNo: String,
        user: SecurityUser,
    ) {
        require(userPrivilegesService.canDelete(user.email, accNo)) {
            throw UserCanNotDeleteSubmission(user.email, accNo)
        }

        delete(accNo, user.email)
    }

    suspend fun deleteSubmissions(
        submissions: List<String>,
        user: SecurityUser,
    ) {
        submissions
            .filter { !userPrivilegesService.canDelete(user.email, it) }
            .ifNotEmpty { throw UserCanNotDeleteSubmissions(user.email, it) }

        submissions.forEach { delete(it, user.email) }
    }

    private suspend fun delete(
        accNo: String,
        user: String,
    ) {
        fileStorageService.deleteSubmissionFiles(queryService.getExtByAccNo(accNo, includeFileListFiles = true))
        submissionPersistenceService.expireSubmission(accNo)
        eventsPublisherService.submissionsRefresh(accNo, user)
    }

    companion object {
        internal const val SYNC_SUBMIT_TIMEOUT = 5L
    }
}
