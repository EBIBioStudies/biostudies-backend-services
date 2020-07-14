package ac.uk.ebi.biostd.handlers.listeners

import ac.uk.ebi.biostd.handlers.NOTIFICATIONS_QUEUE
import ac.uk.ebi.biostd.handlers.api.BioStudiesWebConsumer
import ebi.ac.uk.extended.events.SubmissionSubmitted
import ebi.ac.uk.notifications.service.RtNotificationService
import mu.KotlinLogging
import org.springframework.amqp.rabbit.annotation.RabbitListener

private val logger = KotlinLogging.logger {}

class SubmissionNotificationsListener(
    private val webConsumer: BioStudiesWebConsumer,
    private val rtNotificationService: RtNotificationService
) {
    @RabbitListener(queues = [NOTIFICATIONS_QUEUE])
    fun receiveMessage(message: SubmissionSubmitted) {
        logger.info { "notification for ${ message.accNo }" }

        // TODO decide whether or not to notify based on the user notifications flag
        val submission = webConsumer.getExtSubmission(message.extTabUrl)
        rtNotificationService.notifySuccessfulSubmission(submission, message.ownerFullName, message.uiUrl)
    }
}
