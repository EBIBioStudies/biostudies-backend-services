package ac.uk.ebi.biostd.notifications.templates

import ebi.ac.uk.notifications.integration.model.NotificationTemplate
import ebi.ac.uk.notifications.integration.model.NotificationTemplateModel

internal class PasswordResetTemplate(templateContent: String) : NotificationTemplate<PasswordResetModel>(templateContent)

internal class PasswordResetModel(
    private val mailto: String,
    private val activationLink: String,
    private val username: String) : NotificationTemplateModel {

    override fun getParams() = listOf("USERNAME" to username, "URL" to activationLink, "MAILTO" to mailto)
}
