package ac.uk.ebi.biostd.submission.domain.service

import ac.uk.ebi.biostd.common.config.SUBMISSION_REQUEST_QUEUE
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.filesystem.api.FilesService
import ac.uk.ebi.biostd.submission.exceptions.UserCanNotDelete
import ac.uk.ebi.biostd.submission.exceptions.UserCanNotRelease
import ac.uk.ebi.biostd.submission.model.ReleaseRequest
import ac.uk.ebi.biostd.submission.model.SubmitRequest
import ac.uk.ebi.biostd.submission.submitter.ExtSubmissionSubmitter
import ac.uk.ebi.biostd.submission.submitter.SubmissionSubmitter
import ebi.ac.uk.extended.events.FailedSubmissionRequestMessage
import ebi.ac.uk.extended.events.SubmissionRequestMessage
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import ebi.ac.uk.security.integration.model.api.SecurityUser
import mu.KotlinLogging
import org.springframework.amqp.rabbit.annotation.RabbitListener
import uk.ac.ebi.events.service.EventsPublisherService

private val logger = KotlinLogging.logger {}

class SubmissionService(
    private val queryService: SubmissionPersistenceQueryService,
    private val userPrivilegesService: IUserPrivilegesService,
    private val extSubmissionSubmitter: ExtSubmissionSubmitter,
    private val submissionSubmitter: SubmissionSubmitter,
    private val eventsPublisherService: EventsPublisherService,
    private val fileService: FilesService,
) {
    fun submit(rqt: SubmitRequest): ExtSubmission {
        logger.info { "${rqt.accNo} ${rqt.owner} Received sync submit request for submission ${rqt.accNo}" }
        val submission = submissionSubmitter.submit(rqt)
        eventsPublisherService.submissionSubmitted(submission)
        return submission
    }

    fun submitAsync(rqt: SubmitRequest) {
        logger.info { "${rqt.accNo} ${rqt.owner} Received async submit request for submission ${rqt.accNo}" }

        val (accNo, version) = submissionSubmitter.submitAsync(rqt)
        eventsPublisherService.submissionRequested(accNo, version)
    }

    @RabbitListener(queues = [SUBMISSION_REQUEST_QUEUE], concurrency = "1-2")
    fun processSubmission(request: SubmissionRequestMessage) {
        val (accNo, version) = request
        logger.info { "$accNo, Received process message for submission $accNo, version: $version" }
        runCatching { processSubmission(accNo, version) }.onFailure { onError(it, accNo, version) }
    }

    private fun processSubmission(accNo: String, version: Int): ExtSubmission {
        val processed = extSubmissionSubmitter.processRequest(accNo, version)
        eventsPublisherService.submissionSubmitted(processed)
        return processed
    }

    private fun onError(exception: Throwable, accNo: String, version: Int) {
        val message = FailedSubmissionRequestMessage(accNo, version)
        logger.error(exception) { "$accNo, Problem processing submission request '$accNo': ${exception.message}" }
        eventsPublisherService.submissionFailed(message)
    }

    fun deleteSubmission(accNo: String, user: SecurityUser) {
        require(userPrivilegesService.canDelete(user.email, accNo)) { throw UserCanNotDelete(accNo, user.email) }
        fileService.cleanSubmissionFiles(queryService.getExtByAccNo(accNo, true))
        queryService.expireSubmission(accNo)
    }

    fun deleteSubmissions(submissions: List<String>, user: SecurityUser) {
        submissions.forEach { require(userPrivilegesService.canDelete(user.email, it)) }
        submissions.forEach { deleteSubmission(it, user) }
    }

    fun releaseSubmission(request: ReleaseRequest, user: SecurityUser) {
        require(userPrivilegesService.canRelease(user.email)) { throw UserCanNotRelease(request.accNo, user.email) }
        extSubmissionSubmitter.release(queryService.getExtByAccNo(request.accNo, true))
    }
}
