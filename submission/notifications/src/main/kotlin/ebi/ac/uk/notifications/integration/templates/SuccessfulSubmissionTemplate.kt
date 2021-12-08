package ebi.ac.uk.notifications.integration.templates

import ebi.ac.uk.base.isNotBlank
import ebi.ac.uk.notifications.integration.model.NotificationTemplate
import ebi.ac.uk.notifications.integration.model.NotificationTemplateModel

internal class SuccessfulSubmissionTemplate(
    templateContent: String
) : NotificationTemplate<SuccessfulSubmissionModel>(templateContent)

@Suppress("LongParameterList")
internal class SuccessfulSubmissionModel(
    private val mailto: String,
    private val uiUrl: String,
    private val username: String,
    private val accNo: String,
    private val secretKey: String,
    private val released: Boolean,
    private val title: String?,
    private val releaseDate: String?
) : NotificationTemplateModel {
    override fun getParams(): List<Pair<String, String>> = listOf(
        "ACC_NO" to accNo,
        "USERNAME" to username,
        "TITLE" to title.orEmpty(),
        "MAIL_TO" to mailto,
        "UI_URL" to uiUrl,
        "RELEASE_MESSAGE" to releaseMessage()
    )

    // TODO add template engine
    private fun releaseMessage(): String {
        val submissionUrl = "$uiUrl/studies/$accNo?key=$secretKey"
        val link = "You will be able to see it only by logging in or by accessing it through this link: $submissionUrl"
        val private = "The release date of this study is not set so it's not publicly available. $link"
        val privateWithReleaseDate =
            "The release date of this study is set to $releaseDate and it will be publicly available after that. $link"

        return when {
            released.not().and(releaseDate.isNotBlank()) -> privateWithReleaseDate
            released.not().and(releaseDate.isNullOrBlank()) -> private
            else -> ""
        }
    }
}
