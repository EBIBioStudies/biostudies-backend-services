package ac.uk.ebi.biostd.notifications.templates

import ebi.ac.uk.base.isNotBlank
import ebi.ac.uk.notifications.integration.model.NotificationTemplate
import ebi.ac.uk.notifications.integration.model.NotificationTemplateModel

internal class SuccessfulSubmissionTemplate(content: String) : NotificationTemplate<SuccessfulSubmissionModel>(content)

internal class SuccessfulSubmissionModel(
    private val mailto: String,
    private val username: String,
    private val accNo: String,
    private val secretKey: String,
    private val released: Boolean,
    private val title: String?,
    private val releaseDate: String?
) : NotificationTemplateModel {
    override fun getParams(): List<Pair<String, String>> {
        val isPrivate = if (released.not().and(releaseDate.isNullOrBlank())) "block" else "none"
        val isPrivateReleaseDate = if (released.not().and(releaseDate.isNotBlank())) "block" else "none"

        return listOf("MAILTO" to mailto,
            "USERNAME" to username,
            "ACC_NO" to accNo,
            "TITLE" to (title ?: ""),
            "SECRET_KEY" to secretKey,
            "RELEASE_DATE" to (releaseDate ?: ""),
            "IS_PRIVATE" to isPrivate,
            "IS_PRIVATE_RELEASE_DATE" to isPrivateReleaseDate)
    }
}
