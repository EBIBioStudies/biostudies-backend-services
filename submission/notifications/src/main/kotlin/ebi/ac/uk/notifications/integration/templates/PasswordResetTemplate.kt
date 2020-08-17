package ebi.ac.uk.notifications.integration.templates

import ebi.ac.uk.notifications.integration.model.NotificationTemplate
import ebi.ac.uk.notifications.integration.model.NotificationTemplateModel

internal class PasswordResetTemplate(
    templateContent: String
) : NotificationTemplate<PasswordResetModel>(templateContent)

internal class PasswordResetModel(
    private val mailto: String,
    private val activationLink: String,
    private val username: String
) : NotificationTemplateModel {

    override fun getParams(): List<Pair<String, String>> =
        listOf("USERNAME" to username, "URL" to activationLink, "MAILTO" to mailto)
}
