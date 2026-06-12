package ebi.ac.uk.notifications.integration.templates

import ebi.ac.uk.notifications.integration.model.NotificationTemplate
import ebi.ac.uk.notifications.integration.model.NotificationTemplateModel

internal class CleanUpNotificationTemplate(
    templateContent: String,
) : NotificationTemplate<CleanUpNotificationModel>(templateContent)

internal class CleanUpNotificationModel(
    private val username: String,
    private val lastActivityDate: String,
    private val cleanUpDate: String,
) : NotificationTemplateModel {
    override fun getParams(): List<Pair<String, String>> =
        listOf(
            "USERNAME" to username,
            "LAST_ACTIVITY_DATE" to lastActivityDate,
            "CLEAN_UP_DATE" to cleanUpDate,
        )
}
