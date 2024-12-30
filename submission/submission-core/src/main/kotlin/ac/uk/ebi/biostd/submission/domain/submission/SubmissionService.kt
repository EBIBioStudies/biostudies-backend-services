package ac.uk.ebi.biostd.submission.domain.submission

import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.api.FileStorageService
import ac.uk.ebi.biostd.submission.exceptions.UserCanNotDeleteSubmission
import ac.uk.ebi.biostd.submission.exceptions.UserCanNotDeleteSubmissions
import ac.uk.ebi.biostd.submission.model.SubmitRequest
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.model.SubmissionId
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import ebi.ac.uk.security.integration.model.api.SecurityUser
import ebi.ac.uk.util.collections.ifNotEmpty
import mu.KotlinLogging
import uk.ac.ebi.events.service.EventsPublisherService

private val logger = KotlinLogging.logger {}

@Suppress("LongParameterList")
class SubmissionService(
    private val queryService: SubmissionPersistenceQueryService,
    private val userPrivilegesService: IUserPrivilegesService,
    private val submitter: SubmissionSubmitter,
    private val eventsPublisher: EventsPublisherService,
    private val submissionPersistenceService: SubmissionPersistenceService,
    private val fileStorageService: FileStorageService,
) {
    suspend fun submit(rqt: SubmitRequest): ExtSubmission {
        logger.info { "${rqt.accNo} ${rqt.owner} Received sync submit request with draft key '${rqt.draftKey}'" }

        val (accNo, version) = submitter.createRqt(rqt)
        return submitter.handleRequest(accNo, version)
    }

    suspend fun submitAsync(rqt: SubmitRequest): SubmissionId {
        logger.info { "${rqt.accNo} ${rqt.owner} Received async submit request with draft key '${rqt.draftKey}'" }
        val (accNo, version) = submitter.createRqt(rqt)
        submitter.handleRequestAsync(accNo, version)
        return SubmissionId(accNo, version)
    }

    suspend fun submitAsync(rqt: List<SubmitRequest>): List<SubmissionId> {
        val requests = rqt.map { submitter.createRqt(it) }.map { SubmissionId(it.accNo, it.version) }
        submitter.handleManyAsync(requests)
        return requests
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
        eventsPublisher.submissionsRefresh(accNo, user)
    }

    companion object {
        internal const val SYNC_SUBMIT_TIMEOUT = 5L
    }
}
