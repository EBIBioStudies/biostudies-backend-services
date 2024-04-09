package ebi.ac.uk.notifications.integration.templates

import ebi.ac.uk.notifications.integration.model.NotificationTemplate
import ebi.ac.uk.notifications.integration.model.NotificationTemplateModel

internal class NotificationSubjectTemplate(content: String) : NotificationTemplate<NotificationSubjectModel>(content)

internal class NotificationSubjectModel(
    private val accNo: String,
) : NotificationTemplateModel {
    override fun getParams(): List<Pair<String, String>> =
        listOf(
            "ACC_NO" to accNo,
        )
}
