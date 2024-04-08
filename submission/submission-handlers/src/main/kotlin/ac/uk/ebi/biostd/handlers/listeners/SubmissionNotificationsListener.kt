package ac.uk.ebi.biostd.handlers.listeners

import ac.uk.ebi.biostd.common.events.BIOSTUDIES_EXCHANGE
import ac.uk.ebi.biostd.common.properties.NotificationProperties
import ac.uk.ebi.biostd.handlers.api.BioStudiesWebConsumer
import ac.uk.ebi.biostd.handlers.common.HANDLERS_SUBSYSTEM
import ac.uk.ebi.biostd.handlers.common.SYSTEM_NAME
import ac.uk.ebi.biostd.handlers.config.FAILED_SUBMISSIONS_NOTIFICATIONS_QUEUE
import ac.uk.ebi.biostd.handlers.config.NOTIFICATIONS_FAILED_REQUEST_ROUTING_KEY
import ac.uk.ebi.biostd.handlers.config.RELEASE_NOTIFICATIONS_QUEUE
import ac.uk.ebi.biostd.handlers.config.SUBMIT_NOTIFICATIONS_QUEUE
import ebi.ac.uk.commons.http.slack.Alert
import ebi.ac.uk.commons.http.slack.NotificationsSender
import ebi.ac.uk.extended.events.FailedRequestMessage
import ebi.ac.uk.extended.events.SubmissionMessage
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.notifications.service.RtNotificationService
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate

class SubmissionNotificationsListener(
    private val rabbitTemplate: RabbitTemplate,
    private val webConsumer: BioStudiesWebConsumer,
    private val notificationsSender: NotificationsSender,
    private val rtNotificationService: RtNotificationService,
    private val notificationProps: NotificationProperties,
) {
    @RabbitListener(queues = [SUBMIT_NOTIFICATIONS_QUEUE])
    fun receiveSubmissionMessage(message: SubmissionMessage) {
        logger.info { "Submission Notification for ${message.accNo}" }

        notifySafely(message, SUCCESSFUL_SUBMISSION_NOTIFICATION) {
            val owner = webConsumer.getExtUser(message.extUserUrl)
            if (owner.notificationsEnabled) {
                val sub = webConsumer.getExtSubmission(message.extTabUrl)
                val uiUrl = getUiUrl(sub)
                rtNotificationService.notifySuccessfulSubmission(sub, owner.fullName, uiUrl, notificationProps.stUrl)
            }
        }
    }

    @RabbitListener(queues = [RELEASE_NOTIFICATIONS_QUEUE])
    fun receiveSubmissionReleaseMessage(message: SubmissionMessage) {
        logger.info { "Release notification for ${message.accNo}" }

        notifySafely(message, RELEASE_NOTIFICATION) {
            val owner = webConsumer.getExtUser(message.extUserUrl)
            if (owner.notificationsEnabled) {
                val sub = webConsumer.getExtSubmission(message.extTabUrl)
                val uiUrl = getUiUrl(sub)
                rtNotificationService.notifySubmissionRelease(sub, owner.fullName, uiUrl, notificationProps.stUrl)
            }
        }
    }

    @RabbitListener(queues = [FAILED_SUBMISSIONS_NOTIFICATIONS_QUEUE])
    fun receiveFailedSubmissionMessage(msg: FailedRequestMessage) {
        val errorMessage = "Problem processing submission '${msg.accNo}' with version ${msg.version}."
        runBlocking {
            notificationsSender.send(Alert(SYSTEM_NAME, HANDLERS_SUBSYSTEM, errorMessage))
        }
    }

    private fun notifySafely(
        message: SubmissionMessage,
        notificationType: String,
        notifyFunction: SubmissionMessage.() -> Unit,
    ) = runBlocking {
        runCatching {
            notifyFunction(message)
        }.onFailure {
            onError(message)
            val errorMsg = "Error processing notification of type '$notificationType' for submission '${message.accNo}"
            logger.error(it) { "$errorMsg': ${it.message ?: it.localizedMessage}" }
        }
    }

    private suspend fun onError(message: SubmissionMessage) {
        rabbitTemplate.convertAndSend(BIOSTUDIES_EXCHANGE, NOTIFICATIONS_FAILED_REQUEST_ROUTING_KEY, message)
        notificationsSender.send(Alert(SYSTEM_NAME, HANDLERS_SUBSYSTEM, String.format(ERROR_MESSAGE, message.accNo)))
    }

    private fun getUiUrl(submission: ExtSubmission): String {
        return when (val col = submission.collections.firstOrNull()) {
            null -> notificationProps.uiUrl
            else -> "${notificationProps.uiUrl}/${col.accNo.lowercase()}"
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}

        private const val ERROR_MESSAGE = "Problem processing notification for submission %s"
        private const val RELEASE_NOTIFICATION = "Release Notification"
        private const val SUCCESSFUL_SUBMISSION_NOTIFICATION = "Successful Submission Notification"
    }
}
