package ac.uk.ebi.biostd.notifications.templates

import ebi.ac.uk.notifications.integration.model.NotificationTemplate
import ebi.ac.uk.notifications.integration.model.NotificationTemplateModel

internal class SuccessfulSubmissionTemplate(content: String) : NotificationTemplate<SuccessfulSubmissionModel>(content)

internal class SuccessfulSubmissionModel(
    private val mailto: String,
    private val accNo: String
) : NotificationTemplateModel {
    override fun getParams(): List<Pair<String, String>> = listOf("ACCNO" to accNo, "MAILTO" to mailto)
}
