package ac.uk.ebi.biostd.submission.domain.service

import ac.uk.ebi.biostd.common.config.LISTENER_FACTORY_NAME
import ac.uk.ebi.biostd.common.config.SUBMISSION_REQUEST_QUEUE
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.api.FileStorageService
import ac.uk.ebi.biostd.submission.exceptions.UserCanNotDelete
import ac.uk.ebi.biostd.submission.exceptions.UserCanNotRelease
import ac.uk.ebi.biostd.submission.model.ReleaseRequest
import ac.uk.ebi.biostd.submission.model.SubmitRequest
import ac.uk.ebi.biostd.submission.submitter.ExtSubmissionSubmitter
import ac.uk.ebi.biostd.submission.submitter.SubmissionSubmitter
import ebi.ac.uk.extended.events.RequestCleaned
import ebi.ac.uk.extended.events.RequestCreated
import ebi.ac.uk.extended.events.RequestLoaded
import ebi.ac.uk.extended.events.RequestMessage
import ebi.ac.uk.extended.events.RequestProcessed
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import ebi.ac.uk.security.integration.model.api.SecurityUser
import mu.KotlinLogging
import org.springframework.amqp.rabbit.annotation.RabbitHandler
import org.springframework.amqp.rabbit.annotation.RabbitListener
import uk.ac.ebi.events.service.EventsPublisherService

private val logger = KotlinLogging.logger {}

@Suppress("LongParameterList", "TooManyFunctions")
@RabbitListener(queues = [SUBMISSION_REQUEST_QUEUE], containerFactory = LISTENER_FACTORY_NAME)
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
        logger.info { "${rqt.accNo} ${rqt.owner} Received sync submit request for submission ${rqt.accNo}" }
        val submission = submissionSubmitter.submit(rqt)
        eventsPublisherService.submissionSubmitted(submission.accNo, submission.owner)
        return submission
    }

    fun submitAsync(rqt: SubmitRequest) {
        logger.info { "${rqt.accNo} ${rqt.owner} Received async submit request for submission ${rqt.accNo}" }
        val (accNo, version) = submissionSubmitter.createRequest(rqt)
        eventsPublisherService.requestCreated(accNo, version)
    }

    @RabbitHandler
    fun loadRequest(rqt: RequestCreated) {
        processSafely(rqt) {
            logger.info { "$accNo, received Created message for submission $accNo, version: $accNo" }
            val submission = submissionSubmitter.loadRequest(rqt)
            eventsPublisherService.requestLoaded(submission.accNo, submission.version)
        }
    }

    @RabbitHandler
    fun cleanRequest(rqt: RequestLoaded) {
        processSafely(rqt) {
            logger.info { "$accNo, received Loaded message for submission $accNo, version: $accNo" }
            submissionSubmitter.cleanRequest(rqt)
            eventsPublisherService.requestCleaned(rqt.accNo, rqt.version)
        }
    }

    @RabbitHandler
    fun processRequest(rqt: RequestCleaned) {
        processSafely(rqt) {
            logger.info { "$accNo, received Cleaned message for submission $accNo, version: $accNo" }
            val submission = submissionSubmitter.processRequest(rqt)
            eventsPublisherService.requestProcessed(submission.accNo, submission.version)
        }
    }

    @RabbitHandler
    fun checkReleased(rqt: RequestProcessed) {
        processSafely(rqt) {
            logger.info { "$accNo, received Processed message for submission $accNo, version: $accNo" }
            val submission = submissionSubmitter.checkReleased(rqt)
            eventsPublisherService.submissionSubmitted(submission.accNo, submission.owner)
        }
    }

    private fun processSafely(request: RequestMessage, process: RequestMessage.() -> Unit) {
        runCatching { process(request) }.onFailure { onError(it, request) }
    }

    private fun onError(exception: Throwable, rqt: RequestMessage) {
        logger.error(exception) { "${rqt.accNo}, Problem processing request '${rqt.accNo}': ${exception.message}" }
        eventsPublisherService.submissionFailed(rqt)
    }

    fun deleteSubmission(accNo: String, user: SecurityUser) {
        require(userPrivilegesService.canDelete(user.email, accNo)) { throw UserCanNotDelete(accNo, user.email) }
        fileStorageService.cleanSubmissionFiles(queryService.getExtByAccNo(accNo, true))
        submissionPersistenceService.expireSubmission(accNo)
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
