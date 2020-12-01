package ac.uk.ebi.biostd.submission.domain.service

import ac.uk.ebi.biostd.common.config.SUBMISSION_REQUEST_QUEUE
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat.JsonFormat.JsonPretty
import ac.uk.ebi.biostd.integration.SubFormat.TsvFormat.Tsv
import ac.uk.ebi.biostd.integration.SubFormat.XmlFormat
import ac.uk.ebi.biostd.persistence.common.model.BasicSubmission
import ac.uk.ebi.biostd.persistence.common.request.SaveSubmissionRequest
import ac.uk.ebi.biostd.persistence.common.request.SubmissionFilter
import ac.uk.ebi.biostd.persistence.common.service.SubmissionMetaQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.submission.ext.getSimpleByAccNo
import ac.uk.ebi.biostd.submission.model.SubmissionRequest
import ac.uk.ebi.biostd.submission.submitter.SubmissionSubmitter
import ebi.ac.uk.extended.events.SubmissionRequestMessage
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import ebi.ac.uk.security.integration.model.api.SecurityUser
import mu.KotlinLogging
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import uk.ac.ebi.events.config.BIOSTUDIES_EXCHANGE
import uk.ac.ebi.events.config.SUBMISSIONS_REQUEST_ROUTING_KEY
import uk.ac.ebi.events.service.EventsPublisherService

private val logger = KotlinLogging.logger {}

@Suppress("TooManyFunctions")
class SubmissionService(
    private val submissionQueryService: SubmissionQueryService,
    private val serializationService: SerializationService,
    private val userPrivilegesService: IUserPrivilegesService,
    private val queryService: SubmissionMetaQueryService,
    private val submissionSubmitter: SubmissionSubmitter,
    private val eventsPublisherService: EventsPublisherService,
    private val myRabbitTemplate: RabbitTemplate
) {
    fun submit(request: SubmissionRequest): ExtSubmission {
        logger.info { "received submit request for submission ${request.submission.accNo}" }

        val extSubmission = submissionSubmitter.submit(request)
        eventsPublisherService.submissionSubmitted(extSubmission)

        return extSubmission
    }

    fun submitAsync(request: SubmissionRequest) {
        logger.info { "received async submit  request for submission ${request.submission.accNo}" }
        val (extSubmission, mode) = submissionSubmitter.submitAsync(request)
        myRabbitTemplate.convertAndSend(
            BIOSTUDIES_EXCHANGE,
            SUBMISSIONS_REQUEST_ROUTING_KEY,
            SubmissionRequestMessage(extSubmission.accNo, extSubmission.version, mode)
        )
    }

    @RabbitListener(queues = [SUBMISSION_REQUEST_QUEUE], concurrency = "1-1")
    fun processSubmission(request: SubmissionRequestMessage) {
        logger.info { "received process message for submission ${request.accNo} , version: ${request.version}" }
        val submission = getRequest(request.accNo, request.version)
        val processed = submissionSubmitter.processRequest(SaveSubmissionRequest(submission, request.fileMode))
        eventsPublisherService.submissionSubmitted(processed)
    }

    fun getSubmissionAsJson(accNo: String): String {
        val submission = submissionQueryService.getSimpleByAccNo(accNo)
        return serializationService.serializeSubmission(submission, JsonPretty)
    }

    fun getSubmissionAsXml(accNo: String): String {
        val submission = submissionQueryService.getSimpleByAccNo(accNo)
        return serializationService.serializeSubmission(submission, XmlFormat)
    }

    fun getSubmissionAsTsv(accNo: String): String {
        val submission = submissionQueryService.getSimpleByAccNo(accNo)
        return serializationService.serializeSubmission(submission, Tsv)
    }

    fun getSubmissions(user: SecurityUser, filter: SubmissionFilter): List<BasicSubmission> =
        submissionQueryService.getSubmissionsByUser(user.id, filter)

    fun deleteSubmission(accNo: String, user: SecurityUser) {
        require(userPrivilegesService.canDelete(user.email, accNo))
        submissionQueryService.expireSubmission(accNo)
    }

    fun getSubmission(accNo: String): ExtSubmission = submissionQueryService.getExtByAccNo(accNo)

    private fun getRequest(accNo: String, version: Int) = submissionQueryService.getRequest(accNo, version)

    fun findPreviousVersion(accNo: String): BasicSubmission? = queryService.findLatestBasicByAccNo(accNo)
}
