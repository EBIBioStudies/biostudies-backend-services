package uk.ac.ebi.events.service

import ac.uk.ebi.biostd.common.events.BIOSTUDIES_EXCHANGE
import ac.uk.ebi.biostd.common.events.SECURITY_NOTIFICATIONS_ROUTING_KEY
import ac.uk.ebi.biostd.common.events.SUBMISSIONS_FAILED_REQUEST_ROUTING_KEY
import ac.uk.ebi.biostd.common.events.SUBMISSIONS_PARTIAL_UPDATE_ROUTING_KEY
import ac.uk.ebi.biostd.common.events.SUBMISSIONS_ROUTING_KEY
import ac.uk.ebi.biostd.common.properties.NotificationsProperties
import ebi.ac.uk.extended.events.RequestCheckedReleased
import ebi.ac.uk.extended.events.RequestCleaned
import ebi.ac.uk.extended.events.RequestCreated
import ebi.ac.uk.extended.events.RequestFilesCopied
import ebi.ac.uk.extended.events.RequestFinalized
import ebi.ac.uk.extended.events.RequestIndexed
import ebi.ac.uk.extended.events.RequestLoaded
import ebi.ac.uk.extended.events.RequestMessage
import ebi.ac.uk.extended.events.RequestPageTabGenerated
import ebi.ac.uk.extended.events.RequestPersisted
import ebi.ac.uk.extended.events.SecurityNotification
import ebi.ac.uk.extended.events.SubmissionMessage
import org.springframework.amqp.rabbit.core.RabbitTemplate
import uk.ac.ebi.events.config.EventsProperties

@Suppress("TooManyFunctions")
class EventsPublisherService(
    private val rabbitTemplate: RabbitTemplate,
    private val eventsProperties: EventsProperties,
    private val notificationsProperties: NotificationsProperties,
) {
    fun securityNotification(notification: SecurityNotification) =
        rabbitTemplate.convertAndSend(BIOSTUDIES_EXCHANGE, SECURITY_NOTIFICATIONS_ROUTING_KEY, notification)

    fun requestCreated(accNo: String, version: Int) =
        rabbitTemplate.convertAndSend(
            BIOSTUDIES_EXCHANGE, notificationsProperties.requestRoutingKey, RequestCreated(accNo, version)
        )

    fun requestFilesCopied(accNo: String, version: Int) {
        rabbitTemplate.convertAndSend(
            BIOSTUDIES_EXCHANGE, notificationsProperties.requestRoutingKey, RequestFilesCopied(accNo, version)
        )
    }

    fun requestIndexed(accNo: String, version: Int) =
        rabbitTemplate.convertAndSend(
            BIOSTUDIES_EXCHANGE, notificationsProperties.requestRoutingKey, RequestIndexed(accNo, version)
        )

    fun requestLoaded(accNo: String, version: Int) =
        rabbitTemplate.convertAndSend(
            BIOSTUDIES_EXCHANGE, notificationsProperties.requestRoutingKey, RequestLoaded(accNo, version)
        )

    fun requestPageTabGenerated(accNo: String, version: Int) {
        rabbitTemplate.convertAndSend(
            BIOSTUDIES_EXCHANGE, notificationsProperties.requestRoutingKey, RequestPageTabGenerated(accNo, version)
        )
    }

    fun requestCleaned(accNo: String, version: Int) =
        rabbitTemplate.convertAndSend(
            BIOSTUDIES_EXCHANGE, notificationsProperties.requestRoutingKey, RequestCleaned(accNo, version)
        )

    fun checkReleased(accNo: String, version: Int) =
        rabbitTemplate.convertAndSend(
            BIOSTUDIES_EXCHANGE, notificationsProperties.requestRoutingKey, RequestCheckedReleased(accNo, version)
        )

    fun submissionPersisted(accNo: String, version: Int) =
        rabbitTemplate.convertAndSend(
            BIOSTUDIES_EXCHANGE, notificationsProperties.requestRoutingKey, RequestPersisted(accNo, version)
        )

    fun submissionRequest(accNo: String, version: Int) =
        rabbitTemplate.convertAndSend(
            BIOSTUDIES_EXCHANGE, notificationsProperties.requestRoutingKey, RequestCreated(accNo, version)
        )

    fun submissionFinalized(accNo: String, version: Int) =
        rabbitTemplate.convertAndSend(
            BIOSTUDIES_EXCHANGE, notificationsProperties.requestRoutingKey, RequestFinalized(accNo, version)
        )

    fun submissionSubmitted(accNo: String, owner: String) =
        rabbitTemplate.convertAndSend(
            BIOSTUDIES_EXCHANGE, SUBMISSIONS_ROUTING_KEY, submissionMessage(accNo, owner)
        )

    fun submissionFailed(request: RequestMessage) =
        rabbitTemplate.convertAndSend(BIOSTUDIES_EXCHANGE, SUBMISSIONS_FAILED_REQUEST_ROUTING_KEY, request)

    fun submissionsRefresh(accNo: String, owner: String) =
        rabbitTemplate.convertAndSend(
            BIOSTUDIES_EXCHANGE, SUBMISSIONS_PARTIAL_UPDATE_ROUTING_KEY, submissionMessage(accNo, owner)
        )

    private fun submissionMessage(accNo: String, owner: String): SubmissionMessage =
        SubmissionMessage.createNew(accNo, owner, eventsProperties.instanceBaseUrl)
}
