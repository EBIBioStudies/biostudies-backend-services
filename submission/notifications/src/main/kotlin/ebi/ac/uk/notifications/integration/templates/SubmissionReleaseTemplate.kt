package ebi.ac.uk.notifications.integration.templates

import ebi.ac.uk.notifications.integration.model.NotificationTemplate
import ebi.ac.uk.notifications.integration.model.NotificationTemplateModel

internal class SubmissionReleaseTemplate(
    templateContent: String
) : NotificationTemplate<SubmissionReleaseModel>(templateContent)

internal class SubmissionReleaseModel(
    private val mailto: String,
    private val uiUrl: String,
    private val username: String,
    private val accNo: String,
    private val title: String,
    private val releaseDate: String
) : NotificationTemplateModel {
    override fun getParams(): List<Pair<String, String>> = listOf(
        "ACC_NO" to accNo,
        "USERNAME" to username,
        "TITLE" to title,
        "MAIL_TO" to mailto,
        "UI_URL" to uiUrl,
        "RELEASE_DATE" to releaseDate
    )
}
