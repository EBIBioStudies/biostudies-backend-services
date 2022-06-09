package ac.uk.ebi.biostd.handlers.listeners

import ac.uk.ebi.biostd.common.properties.NotificationProperties
import ac.uk.ebi.biostd.handlers.api.BioStudiesWebConsumer
import ac.uk.ebi.biostd.handlers.config.BIOSTUDIES_EXCHANGE
import ac.uk.ebi.biostd.handlers.config.FAILED_SUBMISSIONS_NOTIFICATIONS_QUEUE
import ac.uk.ebi.biostd.handlers.config.NOTIFICATIONS_FAILED_REQUEST_ROUTING_KEY
import ac.uk.ebi.biostd.handlers.config.RELEASE_NOTIFICATIONS_QUEUE
import ac.uk.ebi.biostd.handlers.config.SUBMIT_NOTIFICATIONS_QUEUE
import ebi.ac.uk.commons.http.slack.Alert
import ebi.ac.uk.commons.http.slack.NotificationsSender
import ebi.ac.uk.extended.events.FailedSubmissionRequestMessage
import ebi.ac.uk.extended.events.SubmissionMessage
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.notifications.service.RtNotificationService
import mu.KotlinLogging
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate

private val logger = KotlinLogging.logger {}
private const val SYSTEM_NAME = "Submitter"
private const val HANDLERS_SUBSYSTEM = "Submission Handlers"

class SubmissionNotificationsListener(
    private val rabbitTemplate: RabbitTemplate,
    private val webConsumer: BioStudiesWebConsumer,
    private val notificationsSender: NotificationsSender,
    private val rtNotificationService: RtNotificationService,
    private val notificationProperties: NotificationProperties,
) {
    @RabbitListener(queues = [SUBMIT_NOTIFICATIONS_QUEUE])
    fun receiveSubmissionMessage(message: SubmissionMessage) {
        logger.info { "Submission Notification for ${message.accNo}" }

        notify(
            message,
            "Problem processing notification for submission ${message.accNo}",
            rtNotificationService::notifySuccessfulSubmission,
        )
    }

    @RabbitListener(queues = [RELEASE_NOTIFICATIONS_QUEUE])
    fun receiveSubmissionReleaseMessage(message: SubmissionMessage) {
        logger.info { "Release notification for ${message.accNo}" }

        notify(
            message,
            "Problem processing release notification for submission ${message.accNo}",
            rtNotificationService::notifySubmissionRelease,
        )
    }

    @RabbitListener(queues = [FAILED_SUBMISSIONS_NOTIFICATIONS_QUEUE])
    fun receiveFailedSubmissionMessage(msg: FailedSubmissionRequestMessage): Unit =
        sendErrorNotification("Problem processing submission '${msg.accNo}' with version ${msg.version}.")

    private fun notify(
        message: SubmissionMessage,
        errorMessage: String,
        notifyFunction: (ExtSubmission, String, String) -> Unit,
    ) = runCatching {
        sendNotification(message, notifyFunction)
    }.onFailure {
        logFailedNotification(message)
        sendErrorNotification(errorMessage)
    }

    private fun sendNotification(message: SubmissionMessage, notifyFunction: (ExtSubmission, String, String) -> Unit) {
        val owner = webConsumer.getExtUser(message.extUserUrl)

        if (owner.notificationsEnabled) {
            val submission = webConsumer.getExtSubmission(message.extTabUrl)
            notifyFunction(submission, owner.fullName, notificationProperties.uiUrl)
        }
    }

    private fun logFailedNotification(message: SubmissionMessage) =
        rabbitTemplate.convertAndSend(BIOSTUDIES_EXCHANGE, NOTIFICATIONS_FAILED_REQUEST_ROUTING_KEY, message)

    private fun sendErrorNotification(message: String) =
        notificationsSender.send(Alert(SYSTEM_NAME, HANDLERS_SUBSYSTEM, message))
}
