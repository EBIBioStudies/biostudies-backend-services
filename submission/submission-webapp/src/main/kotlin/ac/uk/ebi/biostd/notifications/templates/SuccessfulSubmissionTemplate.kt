package ac.uk.ebi.biostd.notifications.templates

import ebi.ac.uk.base.isNotBlank
import ebi.ac.uk.notifications.integration.model.NotificationTemplate
import ebi.ac.uk.notifications.integration.model.NotificationTemplateModel

internal class SuccessfulSubmissionTemplate : NotificationTemplate<SuccessfulSubmissionModel>("") {
    override fun getContent(model: SuccessfulSubmissionModel): String = """
        Subject: BioStudies Successful Submission
        Owner: ${model.email}
        Text: Dear ${model.username},
        
         Thank you for submitting your data to BioStudies. Your submission ${model.title} has been assigned BioStudies
         accession number ${model.accNo}.
         
         You'll be able to see it at https://www.ebi.ac.uk/biostudies/studies/${model.accNo} in the next 24 hours.
         ${releaseMessage(model)}
         Should you have any further questions, please contact us at ${model.mailto}
         
         Best regards,
         
         BioStudies Team
    """.trimIndent()

    private fun releaseMessage(model: SuccessfulSubmissionModel): String = when {
        model.released.and(model.releaseDate.isNotBlank()) -> """
             
             The release date of this study is set to ${model.releaseDate} and it will be publicly available after that.
             You will be able to see it only by logging in or by accessing it through this link:
             
             https://www.ebi.ac.uk/biostudies/studies/${model.accNo}/${model.secretKey}
             
        """.trimIndent()

        model.released.and(model.releaseDate.isNullOrBlank()) -> """
             
             The release date of this study is not set so it's not publicly available.  You will be able to see it only
             by logging in or by accessing it through this link:
             
             https://www.ebi.ac.uk/biostudies/studies/${model.accNo}/${model.secretKey}
             
        """.trimIndent()
        else -> ""
    }
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
    override fun getParams(): List<Pair<String, String>> = listOf("ACC_NO" to accNo)
}
