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
    private val fileStorageService: FileStorageService,
    private val submissionPersistenceService: SubmissionPersistenceService,
) {
    fun submit(rqt: SubmitRequest): ExtSubmission {
        logger.info { "${rqt.accNo} ${rqt.owner} Received sync submit request with draft key '${rqt.draftKey}'" }
        return submissionSubmitter.submit(rqt)
    }

    fun submitAsync(rqt: SubmitRequest) {
        logger.info { "${rqt.accNo} ${rqt.owner} Received async submit request with draft key '${rqt.draftKey}'" }
        val (accNo, version) = submissionSubmitter.createRequest(rqt)
        eventsPublisherService.requestCreated(accNo, version)
    }

    fun deleteSubmission(accNo: String, user: SecurityUser) {
        require(userPrivilegesService.canDelete(user.email, accNo)) { throw UserCanNotDelete(accNo, user.email) }
        fileStorageService.cleanSubmissionFiles(queryService.getExtByAccNo(accNo, true))
        submissionPersistenceService.expireSubmission(accNo)
        eventsPublisherService.submissionsRefresh(accNo, user.email)
    }

    fun deleteSubmissions(submissions: List<String>, user: SecurityUser) {
        submissions.forEach { require(userPrivilegesService.canDelete(user.email, it)) }
        submissions.forEach { deleteSubmission(it, user) }
    }

    fun releaseSubmission(request: ReleaseRequest, user: SecurityUser) {
        require(userPrivilegesService.canRelease(user.email)) { throw UserCanNotRelease(request.accNo, user.email) }
        extSubmissionSubmitter.release(request.accNo)
    }
}
