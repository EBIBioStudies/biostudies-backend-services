package uk.ac.ebi.events.service

import ebi.ac.uk.extended.events.RequestSubmitted
import ebi.ac.uk.extended.events.SecurityNotification
import ebi.ac.uk.extended.events.SubmissionSubmitted
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FileMode
import org.springframework.amqp.rabbit.core.RabbitTemplate
import uk.ac.ebi.events.config.BIOSTUDIES_EXCHANGE
import uk.ac.ebi.events.config.EventsProperties
import uk.ac.ebi.events.config.SECURITY_NOTIFICATIONS_ROUTING_KEY
import uk.ac.ebi.events.config.SUBMISSIONS_REQUEST_ROUTING_KEY
import uk.ac.ebi.events.config.SUBMISSIONS_ROUTING_KEY

class EventsPublisherService(
    private val rabbitTemplate: RabbitTemplate,
    private val eventsProperties: EventsProperties
) {
    fun securityNotification(notification: SecurityNotification) {
        rabbitTemplate.convertAndSend(BIOSTUDIES_EXCHANGE, SECURITY_NOTIFICATIONS_ROUTING_KEY, notification)
    }

    fun submissionSubmitted(submission: ExtSubmission, ownerEmail: String) {
        val instanceBaseUrl = eventsProperties.instanceBaseUrl
        val submissionNotification = SubmissionSubmitted(
            accNo = submission.accNo,
            uiUrl = instanceBaseUrl,
            pagetabUrl = "$instanceBaseUrl/submissions/${submission.accNo}.json",
            extTabUrl = "$instanceBaseUrl/submissions/extended/${submission.accNo}",
            extUserUrl = "$instanceBaseUrl/security/users/extended/$ownerEmail")

        rabbitTemplate.convertAndSend(BIOSTUDIES_EXCHANGE, SUBMISSIONS_ROUTING_KEY, submissionNotification)
    }

    fun requestSubmitted(submission: ExtSubmission, fileMode: FileMode) {
        rabbitTemplate.convertAndSend(
            BIOSTUDIES_EXCHANGE,
            SUBMISSIONS_REQUEST_ROUTING_KEY,
            RequestSubmitted(submission, fileMode)
        )
    }
}
