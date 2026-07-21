package ac.uk.ebi.biostd.handlers.listeners

import ac.uk.ebi.biostd.common.events.BIOSTUDIES_EXCHANGE
import ac.uk.ebi.biostd.handlers.common.HANDLERS_SUBSYSTEM
import ac.uk.ebi.biostd.handlers.common.SYSTEM_NAME
import ac.uk.ebi.biostd.handlers.config.NOTIFICATIONS_FAILED_REQUEST_ROUTING_KEY
import ac.uk.ebi.biostd.handlers.config.URGENT_NOTIFICATIONS_QUEUE
import ebi.ac.uk.commons.http.slack.Alert
import ebi.ac.uk.commons.http.slack.NotificationsSender
import ebi.ac.uk.extended.events.UrgentNotification
import ebi.ac.uk.notifications.service.UrgentNotificationService
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate

private val logger = KotlinLogging.logger {}

class UrgentNotificationListener(
    private val rabbitTemplate: RabbitTemplate,
    private val notificationsSender: NotificationsSender,
    private val urgentNotificationService: UrgentNotificationService,
) {
    @RabbitListener(queues = [URGENT_NOTIFICATIONS_QUEUE])
    fun receiveMessage(notification: UrgentNotification) {
        logger.info { "Processing urgent notification '${notification.subject}'" }

        runCatching {
            urgentNotificationService.sendUrgentNotification(notification)
        }.onFailure { onError(notification) }
    }

    private fun onError(notification: UrgentNotification) {
        val message = "Problem processing urgent notification ${notification.subject}"
        logger.error { message }
        rabbitTemplate.convertAndSend(BIOSTUDIES_EXCHANGE, NOTIFICATIONS_FAILED_REQUEST_ROUTING_KEY, notification)
        runBlocking {
            notificationsSender.send(Alert(SYSTEM_NAME, HANDLERS_SUBSYSTEM, message))
        }
    }
}
