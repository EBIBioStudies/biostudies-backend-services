package uk.ac.ebi.events.service

import ebi.ac.uk.extended.events.FailedSubmissionRequestMessage
import ebi.ac.uk.extended.events.SecurityNotification
import ebi.ac.uk.extended.events.SubmissionMessage
import ebi.ac.uk.extended.events.SubmissionRequestMessage
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.util.date.asIsoTime
import org.springframework.amqp.rabbit.core.RabbitTemplate
import uk.ac.ebi.events.config.BIOSTUDIES_EXCHANGE
import uk.ac.ebi.events.config.EventsProperties
import uk.ac.ebi.events.config.SECURITY_NOTIFICATIONS_ROUTING_KEY
import uk.ac.ebi.events.config.SUBMISSIONS_FAILED_REQUEST_ROUTING_KEY
import uk.ac.ebi.events.config.SUBMISSIONS_PARTIAL_UPDATE_ROUTING_KEY
import uk.ac.ebi.events.config.SUBMISSIONS_RELEASE_ROUTING_KEY
import uk.ac.ebi.events.config.SUBMISSIONS_REQUEST_ROUTING_KEY
import uk.ac.ebi.events.config.SUBMISSIONS_ROUTING_KEY
import java.time.OffsetDateTime

class EventsPublisherService(
    private val rabbitTemplate: RabbitTemplate,
    private val eventsProperties: EventsProperties
) {
    fun securityNotification(notification: SecurityNotification) =
        rabbitTemplate.convertAndSend(BIOSTUDIES_EXCHANGE, SECURITY_NOTIFICATIONS_ROUTING_KEY, notification)

    fun submissionRequested(accNo: String, version: Int) =
        rabbitTemplate.convertAndSend(
            BIOSTUDIES_EXCHANGE, SUBMISSIONS_REQUEST_ROUTING_KEY, SubmissionRequestMessage(accNo, version)
        )

    fun submissionSubmitted(submission: ExtSubmission) =
        rabbitTemplate.convertAndSend(
            BIOSTUDIES_EXCHANGE, SUBMISSIONS_ROUTING_KEY, submissionMessage(submission.accNo, submission.owner)
        )

    fun submissionReleased(accNo: String, owner: String) =
        rabbitTemplate.convertAndSend(
            BIOSTUDIES_EXCHANGE, SUBMISSIONS_RELEASE_ROUTING_KEY, submissionMessage(accNo, owner)
        )

    fun submissionFailed(request: FailedSubmissionRequestMessage) =
        rabbitTemplate.convertAndSend(BIOSTUDIES_EXCHANGE, SUBMISSIONS_FAILED_REQUEST_ROUTING_KEY, request)

    fun submissionsRefresh(accNo: String, owner: String) =
        rabbitTemplate.convertAndSend(
            BIOSTUDIES_EXCHANGE, SUBMISSIONS_PARTIAL_UPDATE_ROUTING_KEY, submissionMessage(accNo, owner)
        )

    fun submissionRequest(accNo: String, version: Int) =
        rabbitTemplate.convertAndSend(
            BIOSTUDIES_EXCHANGE, SUBMISSIONS_REQUEST_ROUTING_KEY, SubmissionRequestMessage(accNo, version)
        )

    private fun submissionMessage(accNo: String, owner: String): SubmissionMessage {
        return SubmissionMessage(
            accNo = accNo,
            pagetabUrl = "${eventsProperties.instanceBaseUrl}/submissions/$accNo.json",
            extTabUrl = "${eventsProperties.instanceBaseUrl}/submissions/extended/$accNo",
            extUserUrl = "${eventsProperties.instanceBaseUrl}/security/users/extended/$owner",
            eventTime = OffsetDateTime.now().asIsoTime()
        )
    }
}
