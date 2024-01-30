package ebi.ac.uk.notifications.service

import ac.uk.ebi.biostd.common.properties.NotificationProperties
import ebi.ac.uk.extended.events.SecurityNotification
import ebi.ac.uk.notifications.integration.templates.PasswordResetModel
import ebi.ac.uk.notifications.integration.templates.PasswordResetTemplate
import ebi.ac.uk.notifications.integration.templates.UserActivationModel
import ebi.ac.uk.notifications.integration.templates.UserActivationTemplate
import ebi.ac.uk.notifications.model.Email
import ebi.ac.uk.notifications.util.TemplateLoader

internal const val EMAIL_FROM = "BioStudies <biostudies@ebi.ac.uk>"

class SecurityNotificationService(
    private val templateLoader: TemplateLoader,
    private val simpleEmailService: SimpleEmailService,
    private val properties: NotificationProperties,
) {
    fun sendActivationNotification(notification: SecurityNotification) {
        val templateContent = templateLoader.loadTemplate("security/activation.html")
        val userActivationModel = UserActivationModel(
            mailto = FROM,
            activationLink = notification.activationLink,
            username = notification.username,
            activationCode = notification.activationCode
        )
        val email = Email(
            from = EMAIL_FROM,
            to = notification.email,
            bcc = properties.bccEmail,
            subject = "BioStudies Account Activation",
            content = UserActivationTemplate(templateContent).render(userActivationModel)
        )
        simpleEmailService.send(email)
    }

    fun sendActivationByEmailNotification(notification: SecurityNotification) {
        val templateContent = templateLoader.loadTemplate("security/activation-by-email.html")
        val userActivationModel = UserActivationModel(
            mailto = FROM,
            activationLink = notification.activationLink,
            username = notification.username,
            activationCode = notification.activationCode
        )
        val email = Email(
            from = EMAIL_FROM,
            to = notification.email,
            bcc = properties.bccEmail,
            subject = "BioStudies Account Password Setup",
            content = UserActivationTemplate(templateContent).render(userActivationModel)
        )
        simpleEmailService.send(email)
    }

    fun sendPasswordResetNotification(notification: SecurityNotification) {
        val templateContent = templateLoader.loadTemplate("security/reset-password.html")
        val passwordResetModel = PasswordResetModel(
            mailto = FROM,
            activationLink = notification.activationLink,
            username = notification.username
        )
        val email = Email(
            from = EMAIL_FROM,
            to = notification.email,
            bcc = properties.bccEmail,
            subject = "BioStudies Account Password Reset",
            content = PasswordResetTemplate(templateContent).render(passwordResetModel)
        )

        simpleEmailService.send(email)
    }
}
