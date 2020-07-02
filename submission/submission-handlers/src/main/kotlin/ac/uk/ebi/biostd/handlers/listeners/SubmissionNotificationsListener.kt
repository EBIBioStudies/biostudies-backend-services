package ac.uk.ebi.biostd.handlers.listeners

import ac.uk.ebi.biostd.handlers.NOTIFICATIONS_QUEUE
import ebi.ac.uk.extended.events.SubmissionSubmitted
import ebi.ac.uk.notifications.service.RtNotificationService
import mu.KotlinLogging
import org.springframework.amqp.rabbit.annotation.RabbitListener

private val logger = KotlinLogging.logger {}

class SubmissionNotificationsListener(private val rtNotificationService: RtNotificationService) {
    @RabbitListener(queues = [NOTIFICATIONS_QUEUE])
    fun receiveMessage(submission: SubmissionSubmitted) {
        logger.info { "notification for ${ submission.accNo }" }
        rtNotificationService.notifySuccessfulSubmission(submission)
    }
}
