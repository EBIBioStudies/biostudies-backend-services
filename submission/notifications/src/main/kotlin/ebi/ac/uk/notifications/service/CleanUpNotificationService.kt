package ebi.ac.uk.notifications.service

import ac.uk.ebi.biostd.common.properties.NotificationProperties
import ebi.ac.uk.extended.events.CleanUpNotification
import ebi.ac.uk.notifications.integration.templates.CleanUpNotificationModel
import ebi.ac.uk.notifications.integration.templates.CleanUpNotificationTemplate
import ebi.ac.uk.notifications.model.Email
import ebi.ac.uk.notifications.util.TemplateLoader

class CleanUpNotificationService(
    private val templateLoader: TemplateLoader,
    private val simpleEmailService: SimpleEmailService,
    private val properties: NotificationProperties,
) {
    fun sendCleanUpNotification(notification: CleanUpNotification) {
        val templateContent = templateLoader.loadTemplate("cleanup/${notification.emailTemplate}.html")
        val cleanUpModel =
            CleanUpNotificationModel(
                username = notification.username,
                lastActivityDate = notification.lastActivityDate,
                cleanUpDate = notification.cleanUpDate,
            )
        val email =
            Email(
                from = EMAIL_FROM,
                to = notification.email,
                bcc = properties.bccEmail,
                subject = notification.emailSubject,
                content = CleanUpNotificationTemplate(templateContent).render(cleanUpModel),
            )

        simpleEmailService.send(email)
    }
}
