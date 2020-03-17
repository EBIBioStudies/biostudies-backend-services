package ac.uk.ebi.biostd.notifications.templates

import ebi.ac.uk.notifications.integration.model.NotificationTemplate
import ebi.ac.uk.notifications.integration.model.NotificationTemplateModel

internal class SuccessfulSubmissionTemplate(content: String) : NotificationTemplate<SuccessfulSubmissionModel>(content)

internal class SuccessfulSubmissionModel(
    private val mailto: String,
    private val username: String,
    private val accNo: String,
    private val title: String,
    private val secretKey: String
) : NotificationTemplateModel {
    override fun getParams(): List<Pair<String, String>> =
        listOf("MAILTO" to mailto,
            "USERNAME" to username,
            "ACC_NO" to accNo,
            "TITLE" to title,
            "SECRET_KEY" to secretKey)
}
