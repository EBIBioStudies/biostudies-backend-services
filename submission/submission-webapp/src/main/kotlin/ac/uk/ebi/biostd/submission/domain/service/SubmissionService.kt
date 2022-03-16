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
import ac.uk.ebi.biostd.submission.exceptions.UserCanNotRelease
import ac.uk.ebi.biostd.submission.model.ReleaseRequest
import ac.uk.ebi.biostd.submission.model.SubmitRequest
import ac.uk.ebi.biostd.submission.submitter.SubmissionSubmitter
import ebi.ac.uk.extended.events.FailedSubmissionRequestMessage
import ebi.ac.uk.extended.events.SubmissionRequestMessage
import ebi.ac.uk.extended.mapping.to.ToSubmission
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
    fun submit(rqt: SubmitRequest): ExtSubmission {
        val (accNo, version) = submissionSubmitter.submitAsync(rqt)
        return processSubmission(accNo, version)
    }

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
        runCatching { processSubmission(accNo, version) }.onFailure { onError(it, accNo, version) }
    }

    private fun processSubmission(accNo: String, version: Int): ExtSubmission {
        val processed = submissionSubmitter.processRequest(accNo, version)
        eventsPublisherService.submissionSubmitted(processed)
        return processed
    }

    private fun onError(exception: Throwable, accNo: String, version: Int) {
        val message = FailedSubmissionRequestMessage(accNo, version)
        logger.error(exception) { "$accNo, Problem processing submission request '$accNo': ${exception.message}" }
        eventsPublisherService.submissionFailed(message)
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

    fun releaseSubmission(request: ReleaseRequest, user: SecurityUser) {
        require(userPrivilegesService.canRelease(user.email)) { throw UserCanNotRelease(request.accNo, user.email) }
        submissionSubmitter.release(request)
    }
}
