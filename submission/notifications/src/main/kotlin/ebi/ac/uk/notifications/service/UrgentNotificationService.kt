package ebi.ac.uk.notifications.service

import ac.uk.ebi.biostd.common.properties.NotificationProperties
import ebi.ac.uk.extended.events.UrgentNotification
import ebi.ac.uk.notifications.model.Email

class UrgentNotificationService(
    private val properties: NotificationProperties,
    private val simpleEmailService: SimpleEmailService,
) {
    fun sendUrgentNotification(notification: UrgentNotification) {
        val email =
            Email(
                from = EMAIL_FROM,
                to = properties.slackUrgentChannelEmail,
                subject = notification.subject,
                content = notification.content,
            )

        simpleEmailService.send(email)
    }
}
