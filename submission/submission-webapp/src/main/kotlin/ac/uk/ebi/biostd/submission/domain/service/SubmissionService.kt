package ac.uk.ebi.biostd.submission.domain.service

import ac.uk.ebi.biostd.common.config.SUBMISSION_REQUEST_QUEUE
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat.JsonFormat.JsonPretty
import ac.uk.ebi.biostd.integration.SubFormat.TsvFormat.Tsv
import ac.uk.ebi.biostd.integration.SubFormat.XmlFormat
import ac.uk.ebi.biostd.persistence.common.model.BasicSubmission
import ac.uk.ebi.biostd.persistence.common.request.SaveSubmissionRequest
import ac.uk.ebi.biostd.persistence.common.request.SubmissionFilter
import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.submission.exceptions.UserCanNotDelete
import ac.uk.ebi.biostd.submission.ext.getSimpleByAccNo
import ac.uk.ebi.biostd.submission.model.SubmissionRequest
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
    fun submit(request: SubmissionRequest): ExtSubmission {
        val accNo = request.accNo
        logger.info { "$accNo ${request.owner} Received submit request for submission $accNo" }

        val extSubmission = submissionSubmitter.submit(request)
        eventsPublisherService.submissionSubmitted(extSubmission)

        return extSubmission
    }

    fun submitAsync(request: SubmissionRequest) {
        val accNo = request.accNo
        logger.info { "$accNo ${request.owner} Received async submit request for submission $accNo" }

        val (extSub, mode, draftKey) = submissionSubmitter.submitAsync(request)
        myRabbitTemplate.convertAndSend(
            BIOSTUDIES_EXCHANGE,
            SUBMISSIONS_REQUEST_ROUTING_KEY,
            SubmissionRequestMessage(extSub.accNo, extSub.version, mode, extSub.owner, draftKey)
        )
    }

    @RabbitListener(queues = [SUBMISSION_REQUEST_QUEUE], concurrency = "5-20")
    fun processSubmission(request: SubmissionRequestMessage) {
        val (accNo, version, fileMode, submitter, draftKey) = request
        logger.info { "$accNo $submitter Received process message for submission $accNo, version: $version" }

        runCatching {
            val submission = getRequest(accNo, version)
            val saveRequest = SaveSubmissionRequest(submission, fileMode, draftKey)
            val processed = submissionSubmitter.processRequest(saveRequest)

            eventsPublisherService.submissionSubmitted(processed)
        }.onFailure {
            val message = FailedSubmissionRequestMessage(accNo, version, fileMode, draftKey, it.message)

            logger.error { "$accNo $submitter Problem processing submission request '$accNo': ${it.message}" }
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

    private fun getRequest(accNo: String, version: Int) = submissionQueryService.getRequest(accNo, version)
}
