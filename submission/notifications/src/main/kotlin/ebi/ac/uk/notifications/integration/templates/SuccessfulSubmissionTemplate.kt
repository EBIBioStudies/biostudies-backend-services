package ebi.ac.uk.notifications.integration.templates

import ebi.ac.uk.notifications.integration.model.NotificationTemplate
import ebi.ac.uk.notifications.integration.model.NotificationTemplateModel

internal class SuccessfulSubmissionTemplate(content: String) : NotificationTemplate<SuccessfulSubmissionModel>(content)

@Suppress("LongParameterList")
internal class SuccessfulSubmissionModel(
    private val mailto: String,
    private val uiUrl: String,
    private val username: String,
    private val userEmail: String,
    private val accNo: String,
    private val title: String?,
    private val releaseMessage: String,
    private val releaseDate: String,
) : NotificationTemplateModel {

    override fun getParams(): List<Pair<String, String>> = listOf(
        "ACC_NO" to accNo,
        "USERNAME" to username,
        "USER_EMAIL" to userEmail,
        "TITLE" to title.orEmpty(),
        "MAIL_TO" to mailto,
        "UI_URL" to uiUrl,
        "RELEASE_DATE" to releaseDate,
        "RELEASE_MESSAGE" to releaseMessage
    )
}
