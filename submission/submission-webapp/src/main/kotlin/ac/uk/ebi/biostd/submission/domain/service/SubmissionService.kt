package ac.uk.ebi.biostd.submission.domain.service

import ac.uk.ebi.biostd.common.config.SUBMISSION_REQUEST_QUEUE
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat.JsonFormat.JsonPretty
import ac.uk.ebi.biostd.integration.SubFormat.TsvFormat.Tsv
import ac.uk.ebi.biostd.integration.SubFormat.XmlFormat
import ac.uk.ebi.biostd.persistence.common.model.BasicSubmission
import ac.uk.ebi.biostd.persistence.common.request.SubmissionFilter
import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.submission.exceptions.UserCanNotDelete
import ac.uk.ebi.biostd.submission.ext.getSimpleByAccNo
import ac.uk.ebi.biostd.submission.model.SubmitRequest
import ac.uk.ebi.biostd.submission.submitter.SubmissionSubmitter
import ebi.ac.uk.extended.events.FailedSubmissionRequestMessage
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

@Suppress("TooManyFunctions", "LongParameterList")
class SubmissionService(
    private val submissionQueryService: SubmissionQueryService,
    private val serializationService: SerializationService,
    private val userPrivilegesService: IUserPrivilegesService,
    private val submissionSubmitter: SubmissionSubmitter,
    private val eventsPublisherService: EventsPublisherService,
    private val rabbitTemplate: RabbitTemplate
) {
    fun submit(rqt: SubmitRequest): ExtSubmission = submissionSubmitter.submit(rqt)

    fun submitAsync(rqt: SubmitRequest) {
        logger.info { "${rqt.accNo} ${rqt.owner} Received async submit request for submission ${rqt.accNo}" }

        val (accNo, version) = submissionSubmitter.submitAsync(rqt)
        rabbitTemplate.convertAndSend(
            BIOSTUDIES_EXCHANGE,
            SUBMISSIONS_REQUEST_ROUTING_KEY,
            SubmissionRequestMessage(accNo, version)
        )
    }

    @RabbitListener(queues = [SUBMISSION_REQUEST_QUEUE], concurrency = "1-2")
    fun processSubmission(request: SubmissionRequestMessage) {
        val (accNo, version) = request
        logger.info { "$accNo, Received process message for submission $accNo, version: $version" }

        runCatching {
            val processed = submissionSubmitter.processRequest(accNo, version)
            eventsPublisherService.submissionSubmitted(processed)
        }.onFailure {
            val message = FailedSubmissionRequestMessage(accNo, version)
            logger.error(it) { "$accNo, Problem processing submission request '$accNo': ${it.message}" }
            eventsPublisherService.submissionFailed(message)
        }
    }

    fun getSubmissionAsJson(accNo: String): String =
        serializationService.serializeSubmission(submissionQueryService.getSimpleByAccNo(accNo), JsonPretty)

    fun getSubmissionAsXml(accNo: String): String =
        serializationService.serializeSubmission(submissionQueryService.getSimpleByAccNo(accNo), XmlFormat)

    fun getSubmissionAsTsv(accNo: String): String =
        serializationService.serializeSubmission(submissionQueryService.getSimpleByAccNo(accNo), Tsv)

    fun getSubmissions(
        user: SecurityUser,
        filter: SubmissionFilter
    ): List<BasicSubmission> = submissionQueryService.getSubmissionsByUser(user.email, filter)

    fun deleteSubmission(accNo: String, user: SecurityUser) {
        require(userPrivilegesService.canDelete(user.email, accNo)) { throw UserCanNotDelete(accNo, user.email) }
        submissionQueryService.expireSubmission(accNo)
    }

    fun deleteSubmissions(submissions: List<String>, user: SecurityUser) {
        submissions.forEach { require(userPrivilegesService.canDelete(user.email, it)) }
        submissionQueryService.expireSubmissions(submissions)
    }

    fun getSubmission(accNo: String): ExtSubmission = submissionQueryService.getExtByAccNo(accNo)
}
