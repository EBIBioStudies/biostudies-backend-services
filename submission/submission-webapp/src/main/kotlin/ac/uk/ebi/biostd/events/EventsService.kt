package ac.uk.ebi.biostd.events

import ac.uk.ebi.biostd.common.property.ApplicationProperties
import ebi.ac.uk.extended.events.SubmissionSubmitted
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.security.integration.model.api.SecurityUser
import org.springframework.amqp.rabbit.core.RabbitTemplate

class EventsService(
    private val rabbitTemplate: RabbitTemplate,
    private val properties: ApplicationProperties
) {
    fun submissionSubmitted(submission: ExtSubmission, user: SecurityUser) {
        val submissionNotification = SubmissionSubmitted(
            accNo = submission.accNo,
            uiUrl = properties.instanceBaseUrl,
            pagetabUrl = "${properties.instanceBaseUrl}/submissions/${submission.accNo}.json",
            extTabUrl = "${properties.instanceBaseUrl}/submissions/extended/${submission.accNo}",
            extUserUrl = "${properties.instanceBaseUrl}/security/users/extended/${user.email}")

        rabbitTemplate.convertAndSend(BIOSTUDIES_EXCHANGE, SUBMISSIONS_ROUTING_KEY, submissionNotification)
    }
}
