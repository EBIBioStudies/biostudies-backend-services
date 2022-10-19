package uk.ac.ebi.events.service

import ebi.ac.uk.extended.events.RequestCleaned
import ebi.ac.uk.extended.events.RequestCreated
import ebi.ac.uk.extended.events.RequestIndexed
import ebi.ac.uk.extended.events.RequestLoaded
import ebi.ac.uk.extended.events.RequestMessage
import ebi.ac.uk.extended.events.RequestProcessed
import ebi.ac.uk.extended.events.SecurityNotification
import ebi.ac.uk.extended.events.SubmissionMessage
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

@Suppress("TooManyFunctions")
class EventsPublisherService(
    private val rabbitTemplate: RabbitTemplate,
    private val eventsProperties: EventsProperties,
) {
    fun securityNotification(notification: SecurityNotification) =
        rabbitTemplate.convertAndSend(BIOSTUDIES_EXCHANGE, SECURITY_NOTIFICATIONS_ROUTING_KEY, notification)

    fun requestCreated(accNo: String, version: Int) =
        rabbitTemplate.convertAndSend(
            BIOSTUDIES_EXCHANGE, SUBMISSIONS_REQUEST_ROUTING_KEY, RequestCreated(accNo, version)
        )

    fun requestIndexed(accNo: String, version: Int) =
        rabbitTemplate.convertAndSend(
            BIOSTUDIES_EXCHANGE, SUBMISSIONS_REQUEST_ROUTING_KEY, RequestIndexed(accNo, version)
        )

    fun requestLoaded(accNo: String, version: Int) =
        rabbitTemplate.convertAndSend(
            BIOSTUDIES_EXCHANGE, SUBMISSIONS_REQUEST_ROUTING_KEY, RequestLoaded(accNo, version)
        )

    fun requestCleaned(accNo: String, version: Int) =
        rabbitTemplate.convertAndSend(
            BIOSTUDIES_EXCHANGE, SUBMISSIONS_REQUEST_ROUTING_KEY, RequestCleaned(accNo, version)
        )

    fun requestProcessed(accNo: String, version: Int) =
        rabbitTemplate.convertAndSend(
            BIOSTUDIES_EXCHANGE, SUBMISSIONS_REQUEST_ROUTING_KEY, RequestProcessed(accNo, version)
        )

    fun submissionSubmitted(accNo: String, owner: String) =
        rabbitTemplate.convertAndSend(
            BIOSTUDIES_EXCHANGE, SUBMISSIONS_ROUTING_KEY, submissionMessage(accNo, owner)
        )

    fun submissionReleased(accNo: String, owner: String) =
        rabbitTemplate.convertAndSend(
            BIOSTUDIES_EXCHANGE, SUBMISSIONS_RELEASE_ROUTING_KEY, submissionMessage(accNo, owner)
        )

    fun submissionFailed(request: RequestMessage) =
        rabbitTemplate.convertAndSend(BIOSTUDIES_EXCHANGE, SUBMISSIONS_FAILED_REQUEST_ROUTING_KEY, request)

    fun submissionsRefresh(accNo: String, owner: String) =
        rabbitTemplate.convertAndSend(
            BIOSTUDIES_EXCHANGE, SUBMISSIONS_PARTIAL_UPDATE_ROUTING_KEY, submissionMessage(accNo, owner)
        )

    fun submissionRequest(accNo: String, version: Int) =
        rabbitTemplate.convertAndSend(
            BIOSTUDIES_EXCHANGE, SUBMISSIONS_REQUEST_ROUTING_KEY, RequestCreated(accNo, version)
        )

    private fun submissionMessage(accNo: String, owner: String): SubmissionMessage =
        SubmissionMessage(
            accNo = accNo,
            pagetabUrl = "${eventsProperties.instanceBaseUrl}/submissions/$accNo.json",
            extTabUrl = "${eventsProperties.instanceBaseUrl}/submissions/extended/$accNo",
            extUserUrl = "${eventsProperties.instanceBaseUrl}/security/users/extended/$owner",
            eventTime = OffsetDateTime.now().asIsoTime()
        )
}
