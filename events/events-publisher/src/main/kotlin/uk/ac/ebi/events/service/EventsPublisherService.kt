package uk.ac.ebi.events.service

import ebi.ac.uk.extended.events.SecurityNotification
import ebi.ac.uk.extended.events.SubmissionMessage
import ebi.ac.uk.extended.model.ExtSubmission
import org.springframework.amqp.rabbit.core.RabbitTemplate
import uk.ac.ebi.events.config.BIOSTUDIES_EXCHANGE
import uk.ac.ebi.events.config.EventsProperties
import uk.ac.ebi.events.config.SECURITY_NOTIFICATIONS_ROUTING_KEY
import uk.ac.ebi.events.config.SUBMISSIONS_RELEASE_ROUTING_KEY
import uk.ac.ebi.events.config.SUBMISSIONS_ROUTING_KEY

class EventsPublisherService(
    private val rabbitTemplate: RabbitTemplate,
    private val eventsProperties: EventsProperties
) {
    fun securityNotification(notification: SecurityNotification) =
        rabbitTemplate.convertAndSend(BIOSTUDIES_EXCHANGE, SECURITY_NOTIFICATIONS_ROUTING_KEY, notification)

    fun submissionSubmitted(submission: ExtSubmission) =
        rabbitTemplate.convertAndSend(
            BIOSTUDIES_EXCHANGE, SUBMISSIONS_ROUTING_KEY, submissionMessage(submission))

    fun submissionReleased(submission: ExtSubmission) =
        rabbitTemplate.convertAndSend(
            BIOSTUDIES_EXCHANGE, SUBMISSIONS_RELEASE_ROUTING_KEY, submissionMessage(submission))

    private fun submissionMessage(submission: ExtSubmission) =
        SubmissionMessage(
            accNo = submission.accNo,
            pagetabUrl = "${eventsProperties.instanceBaseUrl}/submissions/${submission.accNo}.json",
            extTabUrl = "${eventsProperties.instanceBaseUrl}/submissions/extended/${submission.accNo}",
            extUserUrl = "${eventsProperties.instanceBaseUrl}/security/users/extended/${submission.submitter}")
}
