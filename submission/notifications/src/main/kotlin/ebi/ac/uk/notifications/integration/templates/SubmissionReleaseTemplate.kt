package ebi.ac.uk.notifications.integration.templates

import ebi.ac.uk.notifications.integration.model.NotificationTemplate
import ebi.ac.uk.notifications.integration.model.NotificationTemplateModel

internal class SubmissionReleaseTemplate(
    templateContent: String,
) : NotificationTemplate<SubmissionReleaseModel>(templateContent)

internal class SubmissionReleaseModel(
    private val mailto: String,
    private val uiUrl: String,
    private val stUrl: String,
    private val username: String,
    private val subDescription: String,
    private val releaseDate: String?,
) : NotificationTemplateModel {
    override fun getParams(): List<Pair<String, String>> =
        listOf(
            "USERNAME" to username,
            "SUB_DESCRIPTION" to subDescription,
            "MAIL_TO" to mailto,
            "UI_URL" to uiUrl,
            "ST_URL" to stUrl,
            "RELEASE_DATE" to releaseDate.orEmpty(),
        )
}
