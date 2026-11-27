package ac.uk.ebi.biostd.handlers.listeners

import ac.uk.ebi.biostd.handlers.common.HANDLERS_SUBSYSTEM
import ac.uk.ebi.biostd.handlers.common.SYSTEM_NAME
import ac.uk.ebi.biostd.handlers.config.NOTIFICATIONS_FAILED_REQUEST_ROUTING_KEY
import ac.uk.ebi.biostd.handlers.config.SECURITY_NOTIFICATIONS_QUEUE
import ebi.ac.uk.notifications.service.SecurityNotificationService
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import uk.ac.ebi.biostd.common.events.BIOSTUDIES_EXCHANGE
import uk.ac.ebi.biostd.commons.http.slack.Alert
import uk.ac.ebi.biostd.commons.http.slack.NotificationsSender
import uk.ac.ebi.biostd.extended.events.SecurityNotification
import uk.ac.ebi.biostd.extended.events.SecurityNotificationType.ACTIVATION
import uk.ac.ebi.biostd.extended.events.SecurityNotificationType.ACTIVATION_BY_EMAIL
import uk.ac.ebi.biostd.extended.events.SecurityNotificationType.PASSWORD_RESET

private val logger = KotlinLogging.logger {}

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
        val email = notification.email
        val type = notification.type.name
        val message = "Problem processing security notification of type $type for user $email"
        logger.error { message }
        rabbitTemplate.convertAndSend(BIOSTUDIES_EXCHANGE, NOTIFICATIONS_FAILED_REQUEST_ROUTING_KEY, notification)
        runBlocking {
            notificationsSender.send(Alert(SYSTEM_NAME, HANDLERS_SUBSYSTEM, message))
        }
    }
}
