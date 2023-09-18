package ac.uk.ebi.biostd.submission.domain.service

import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.api.FileStorageService
import ac.uk.ebi.biostd.submission.exceptions.UserCanNotDelete
import ac.uk.ebi.biostd.submission.exceptions.UserCanNotRelease
import ac.uk.ebi.biostd.submission.model.ReleaseRequest
import ac.uk.ebi.biostd.submission.model.SubmitRequest
import ac.uk.ebi.biostd.submission.submitter.ExtSubmissionSubmitter
import ac.uk.ebi.biostd.submission.submitter.SubmissionSubmitter
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import ebi.ac.uk.security.integration.model.api.SecurityUser
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import uk.ac.ebi.events.service.EventsPublisherService

private val logger = KotlinLogging.logger {}

@Suppress("LongParameterList")
class SubmissionService(
    private val queryService: SubmissionPersistenceQueryService,
    private val userPrivilegesService: IUserPrivilegesService,
    private val extSubmissionSubmitter: ExtSubmissionSubmitter,
    private val submissionSubmitter: SubmissionSubmitter,
    private val eventsPublisherService: EventsPublisherService,
    private val submissionPersistenceService: SubmissionPersistenceService,
    private val fileStorageService: FileStorageService,
) {
    suspend fun submit(rqt: SubmitRequest): ExtSubmission {
        logger.info { "${rqt.accNo} ${rqt.owner} Received sync submit request with draft key '${rqt.draftKey}'" }
        return submissionSubmitter.submit(rqt)
    }

    suspend fun submitAsync(rqt: SubmitRequest) {
        logger.info { "${rqt.accNo} ${rqt.owner} Received async submit request with draft key '${rqt.draftKey}'" }
        val (accNo, version) = submissionSubmitter.createRequest(rqt)
        eventsPublisherService.requestCreated(accNo, version)
    }

    suspend fun deleteSubmission(accNo: String, user: SecurityUser) {
        require(userPrivilegesService.canDelete(user.email, accNo)) { throw UserCanNotDelete(accNo, user.email) }
        runBlocking { fileStorageService.deleteSubmissionFiles(queryService.getExtByAccNo(accNo, true)) }
        submissionPersistenceService.expireSubmission(accNo)
        eventsPublisherService.submissionsRefresh(accNo, user.email)
    }

    suspend fun deleteSubmissions(submissions: List<String>, user: SecurityUser) {
        submissions.forEach { require(userPrivilegesService.canDelete(user.email, it)) }
        submissions.forEach { deleteSubmission(it, user) }
    }

    suspend fun releaseSubmission(request: ReleaseRequest, user: SecurityUser) {
        require(userPrivilegesService.canRelease(user.email)) { throw UserCanNotRelease(request.accNo, user.email) }
        extSubmissionSubmitter.release(request.accNo)
        eventsPublisherService.submissionsRefresh(request.accNo, user.email)
    }
}
