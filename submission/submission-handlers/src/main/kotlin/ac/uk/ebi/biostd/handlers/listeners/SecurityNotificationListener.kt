package ac.uk.ebi.biostd.handlers.listeners

import ac.uk.ebi.biostd.handlers.config.SECURITY_NOTIFICATIONS_QUEUE
import ebi.ac.uk.extended.events.SecurityNotification
import ebi.ac.uk.extended.events.SecurityNotificationType.ACTIVATION
import ebi.ac.uk.extended.events.SecurityNotificationType.PASSWORD_RESET
import ebi.ac.uk.notifications.service.SecurityNotificationService
import mu.KotlinLogging
import org.springframework.amqp.rabbit.annotation.RabbitListener

private val logger = KotlinLogging.logger {}

class SecurityNotificationListener(private val securityNotificationService: SecurityNotificationService) {
    @RabbitListener(queues = [SECURITY_NOTIFICATIONS_QUEUE])
    fun receiveMessage(notification: SecurityNotification) {
        logger.info { "processing ${notification.type} notification for user '${notification.email}'" }

        when (notification.type) {
            ACTIVATION -> securityNotificationService.sendActivationNotification(notification)
            PASSWORD_RESET -> securityNotificationService.sendPasswordResetNotification(notification)
        }
    }
}
