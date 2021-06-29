package ebi.ac.uk.notifications.service

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
    private val simpleEmailService: SimpleEmailService
) {
    fun sendActivationNotification(notification: SecurityNotification) {
        val templateContent = templateLoader.loadTemplate("activation.html")
        val userActivationModel = UserActivationModel(FROM, notification.activationLink, notification.username)
        val content = UserActivationTemplate(templateContent).getContent(userActivationModel)
        val email = Email(EMAIL_FROM, notification.email, "BioStudies Account Activation", content)

        simpleEmailService.send(email)
    }

    fun sendActivationByEmailNotification(notification: SecurityNotification) {
        val templateContent = templateLoader.loadTemplate("activation-by-email.html")
        val userActivationModel = UserActivationModel(FROM, notification.activationLink, notification.username)
        val content = UserActivationTemplate(templateContent).getContent(userActivationModel)
        val email = Email(EMAIL_FROM, notification.email, "BioStudies Account Password Setup", content)

        simpleEmailService.send(email)
    }

    fun sendPasswordResetNotification(notification: SecurityNotification) {
        val templateContent = templateLoader.loadTemplate("reset-password.html")
        val passwordResetModel = PasswordResetModel(FROM, notification.activationLink, notification.username)
        val content = PasswordResetTemplate(templateContent).getContent(passwordResetModel)
        val email = Email(EMAIL_FROM, notification.email, "BioStudies Account Password Reset", content)

        simpleEmailService.send(email)
    }
}
