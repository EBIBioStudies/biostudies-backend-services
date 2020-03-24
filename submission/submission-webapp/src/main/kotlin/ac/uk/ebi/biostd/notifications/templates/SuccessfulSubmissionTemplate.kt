package ac.uk.ebi.biostd.notifications.templates

import ebi.ac.uk.base.isNotBlank
import ebi.ac.uk.notifications.integration.model.NotificationTemplate
import ebi.ac.uk.notifications.integration.model.NotificationTemplateModel

internal class SuccessfulSubmissionTemplate(content: String) : NotificationTemplate<SuccessfulSubmissionModel>(content) {
    // TODO proper content
    // TODO test with own user as owner
    override fun getContent(model: SuccessfulSubmissionModel): String = """
        Subject: Successful Submission ${model.accNo}
        Owner: ${model.email}
        Text: Dear ${model.username}, Your submission ${model.title} has been assigned the AccNo ${model.accNo}
    """.trimIndent()
}

internal class SuccessfulSubmissionModel(
    val mailto: String,
    val email: String,
    val username: String,
    val accNo: String,
    val secretKey: String,
    val released: Boolean,
    val title: String?,
    val releaseDate: String?
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
