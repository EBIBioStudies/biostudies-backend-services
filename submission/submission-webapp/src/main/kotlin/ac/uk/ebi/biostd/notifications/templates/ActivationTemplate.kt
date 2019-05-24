package ac.uk.ebi.biostd.notifications.templates

import ebi.ac.uk.notifications.integration.model.NotificationTemplate
import ebi.ac.uk.notifications.integration.model.NotificationTemplateModel

internal class ActivationTemplate(templateContent: String) : NotificationTemplate<ActivationModel>(templateContent)

internal class ActivationModel(
    private val mailto: String,
    private val activationLink: String,
    private val username: String
) : NotificationTemplateModel {

    override fun getParams(): List<Pair<String, String>> =
        listOf("USERNAME" to username, "URL" to activationLink, "MAILTO" to mailto)
}
