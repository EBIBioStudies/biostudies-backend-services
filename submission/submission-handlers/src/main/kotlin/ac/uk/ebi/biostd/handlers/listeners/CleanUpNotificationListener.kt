package ac.uk.ebi.biostd.handlers.listeners

import ac.uk.ebi.biostd.common.events.BIOSTUDIES_EXCHANGE
import ac.uk.ebi.biostd.handlers.common.HANDLERS_SUBSYSTEM
import ac.uk.ebi.biostd.handlers.common.SYSTEM_NAME
import ac.uk.ebi.biostd.handlers.config.CLEANUP_NOTIFICATIONS_QUEUE
import ac.uk.ebi.biostd.handlers.config.NOTIFICATIONS_FAILED_REQUEST_ROUTING_KEY
import ac.uk.ebi.biostd.persistence.common.service.NotificationErrorDataService
import ebi.ac.uk.commons.http.slack.Alert
import ebi.ac.uk.commons.http.slack.NotificationsSender
import ebi.ac.uk.extended.events.CLEAN_UP_NOTIFICATION
import ebi.ac.uk.extended.events.CleanUpNotification
import ebi.ac.uk.notifications.service.CleanUpNotificationService
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate

private val logger = KotlinLogging.logger {}
private const val ERROR_MESSAGE = "Problem processing cleanup notification for user %s"

class CleanUpNotificationListener(
    private val rabbitTemplate: RabbitTemplate,
    private val notificationsSender: NotificationsSender,
    private val cleanUpNotificationService: CleanUpNotificationService,
    private val notificationErrorService: NotificationErrorDataService,
) {
    @RabbitListener(queues = [CLEANUP_NOTIFICATIONS_QUEUE])
    fun receiveMessage(notification: CleanUpNotification) {
        logger.info { "Processing cleanup notification for user '${notification.email}'" }

        runBlocking {
            runCatching {
                cleanUpNotificationService.sendCleanUpNotification(notification)
            }.onFailure { onError(notification, it) }
        }
    }

    private suspend fun onError(
        notification: CleanUpNotification,
        exception: Throwable,
    ) {
        val message = String.format(ERROR_MESSAGE, notification.email)
        logger.error { message }
        rabbitTemplate.convertAndSend(BIOSTUDIES_EXCHANGE, NOTIFICATIONS_FAILED_REQUEST_ROUTING_KEY, notification)
        notificationsSender.send(Alert(SYSTEM_NAME, HANDLERS_SUBSYSTEM, message))
        notificationErrorService.saveNotificationError(
            key = notification.email,
            messagePayload = message,
            notificationType = CLEAN_UP_NOTIFICATION,
            errorMessage = exception.localizedMessage,
        )
    }
}
