package ebi.ac.uk.notifications.service

import ebi.ac.uk.notifications.model.Email
import uk.ac.ebi.biostd.common.properties.NotificationProperties
import uk.ac.ebi.biostd.extended.events.UrgentNotification

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
