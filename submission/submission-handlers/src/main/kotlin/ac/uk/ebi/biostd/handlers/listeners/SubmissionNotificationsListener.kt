package ac.uk.ebi.biostd.handlers.listeners

import ac.uk.ebi.biostd.handlers.config.SUBMIT_NOTIFICATIONS_QUEUE
import ac.uk.ebi.biostd.handlers.api.BioStudiesWebConsumer
import ac.uk.ebi.biostd.handlers.config.RELEASE_NOTIFICATIONS_QUEUE
import ebi.ac.uk.extended.events.SubmissionMessage
import ebi.ac.uk.notifications.integration.NotificationProperties
import ebi.ac.uk.notifications.service.RtNotificationService
import mu.KotlinLogging
import org.springframework.amqp.rabbit.annotation.RabbitListener

private val logger = KotlinLogging.logger {}

class SubmissionNotificationsListener(
    private val webConsumer: BioStudiesWebConsumer,
    private val rtNotificationService: RtNotificationService,
    private val notificationProperties: NotificationProperties
) {
    @RabbitListener(queues = [SUBMIT_NOTIFICATIONS_QUEUE])
    fun receiveSubmissionMessage(message: SubmissionMessage) {
        logger.info { "notification for ${ message.accNo }" }
        val extUser = webConsumer.getExtUser(message.extUserUrl)

        if (extUser.notificationsEnabled) {
            val submission = webConsumer.getExtSubmission(message.extTabUrl)
            rtNotificationService.notifySuccessfulSubmission(submission, extUser.fullName, notificationProperties.uiUrl)
        }
    }

    @RabbitListener(queues = [RELEASE_NOTIFICATIONS_QUEUE])
    fun receiveReleaseMessage(message: SubmissionMessage) {
        logger.info { "release notification for ${ message.accNo }" }
        val extUser = webConsumer.getExtUser(message.extUserUrl)

        if (extUser.notificationsEnabled) {
            val submission = webConsumer.getExtSubmission(message.extTabUrl)
            rtNotificationService.notifySubmissionRelease(submission, extUser.fullName, notificationProperties.uiUrl)
        }
    }
}
