package ac.uk.ebi.biostd.handlers.listeners

import ac.uk.ebi.biostd.common.events.BIOSTUDIES_EXCHANGE
import ac.uk.ebi.biostd.handlers.common.HANDLERS_SUBSYSTEM
import ac.uk.ebi.biostd.handlers.common.SYSTEM_NAME
import ac.uk.ebi.biostd.handlers.config.NOTIFICATIONS_FAILED_REQUEST_ROUTING_KEY
import ac.uk.ebi.biostd.handlers.config.SECURITY_NOTIFICATIONS_QUEUE
import ebi.ac.uk.commons.http.slack.Alert
import ebi.ac.uk.commons.http.slack.NotificationsSender
import ebi.ac.uk.extended.events.SecurityNotification
import ebi.ac.uk.extended.events.SecurityNotificationType.ACTIVATION
import ebi.ac.uk.extended.events.SecurityNotificationType.ACTIVATION_BY_EMAIL
import ebi.ac.uk.extended.events.SecurityNotificationType.PASSWORD_RESET
import ebi.ac.uk.notifications.service.SecurityNotificationService
import mu.KotlinLogging
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate

private val logger = KotlinLogging.logger {}
private const val ERROR_MESSAGE = "Problem processing security notification of type %s for user %s"

class SecurityNotificationListener(
    private val rabbitTemplate: RabbitTemplate,
    private val notificationsSender: NotificationsSender,
    private val securityNotificationService: SecurityNotificationService,
) {
    @RabbitListener(queues = [SECURITY_NOTIFICATIONS_QUEUE])
    fun receiveMessage(notification: SecurityNotification) {
        logger.info { "Processing ${notification.type} notification for user '${notification.email}'" }

        runCatching {
            when (notification.type) {
                ACTIVATION -> securityNotificationService.sendActivationNotification(notification)
                ACTIVATION_BY_EMAIL -> securityNotificationService.sendActivationByEmailNotification(notification)
                PASSWORD_RESET -> securityNotificationService.sendPasswordResetNotification(notification)
            }
        }.onFailure { onError(notification) }
    }

    private fun onError(notification: SecurityNotification) {
        val message = String.format(ERROR_MESSAGE, notification.type.name, notification.email)
        logger.error { message }
        rabbitTemplate.convertAndSend(BIOSTUDIES_EXCHANGE, NOTIFICATIONS_FAILED_REQUEST_ROUTING_KEY, notification)
        notificationsSender.send(Alert(SYSTEM_NAME, HANDLERS_SUBSYSTEM, message))
    }
}
