package ac.uk.ebi.biostd.events

import ac.uk.ebi.biostd.common.property.ApplicationProperties
import ebi.ac.uk.extended.model.ExtSubmission
import org.springframework.amqp.rabbit.core.RabbitTemplate
import java.io.Serializable

class EventsService(
    private val rabbitTemplate: RabbitTemplate,
    private val properties: ApplicationProperties
) {

    fun submissionSubmitted(submission: ExtSubmission) {
        val submissionNotification = SubmissionSubmitted(
            accNo = submission.accNo,
            pagetabUrl = "${properties.instanceBaseUrl}/submissions/${submission.accNo}.json",
            extTabUrl = "${properties.instanceBaseUrl}/submissions/extended/${submission.accNo}"
        )
        rabbitTemplate.convertAndSend("bio.submission.published", submissionNotification)
    }
}

data class SubmissionSubmitted(val accNo: String, val pagetabUrl: String, val extTabUrl: String) : Serializable
