package ebi.ac.uk.notifications.service

import ebi.ac.uk.base.EMPTY
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.computedTitle
import ebi.ac.uk.notifications.integration.templates.SubmissionReleaseModel
import ebi.ac.uk.notifications.integration.templates.SubmissionReleaseTemplate
import ebi.ac.uk.notifications.integration.templates.SuccessfulSubmissionModel
import ebi.ac.uk.notifications.integration.templates.SuccessfulSubmissionTemplate
import ebi.ac.uk.notifications.util.TemplateLoader
import ebi.ac.uk.util.date.toStringDate

internal const val FROM = "biostudies@ebi.ac.uk"
internal const val SUBMISSION_RELEASE_TEMPLATE = "release/%s.txt"
internal const val SUCCESSFUL_SUBMISSION_TEMPLATE = "submission/%s.txt"
internal const val SUCCESSFUL_RESUBMISSION_TEMPLATE = "resubmission/%s.txt"

class RtNotificationService(
    private val templateLoader: TemplateLoader,
    private val rtTicketService: RtTicketService
) {
    fun notifySuccessfulSubmission(sub: ExtSubmission, ownerFullName: String, uiUrl: String, stUrl: String) {
        val subject = "BioStudies Submission - ${sub.accNo}"
        val template = if (sub.version == 1) SUCCESSFUL_SUBMISSION_TEMPLATE else SUCCESSFUL_RESUBMISSION_TEMPLATE
        val model = successfulSubmissionModel(sub, uiUrl, stUrl, ownerFullName)
        val content = SuccessfulSubmissionTemplate(templateLoader.loadTemplateOrDefault(sub, template)).render(model)

        rtTicketService.saveRtTicket(sub.accNo, subject, sub.owner, content)
    }

    fun notifySubmissionRelease(sub: ExtSubmission, ownerFullName: String, uiUrl: String, stUrl: String) {
        val subject = "BioStudies Submission - ${sub.accNo}"
        val model = submissionReleaseModel(sub, uiUrl, stUrl, ownerFullName)
        val template = templateLoader.loadTemplateOrDefault(sub, SUBMISSION_RELEASE_TEMPLATE)
        val content = SubmissionReleaseTemplate(template).render(model)

        rtTicketService.saveRtTicket(sub.accNo, subject, sub.owner, content)
    }

    companion object {
        private fun submissionReleaseModel(
            submission: ExtSubmission,
            uiUrl: String,
            stUrl: String,
            ownerFullName: String
        ): SubmissionReleaseModel {
            val description = buildString {
                append(submission.accNo)
                submission.computedTitle?.let { append(" - \"$it\"") }
            }

            return SubmissionReleaseModel(
                FROM,
                uiUrl,
                stUrl,
                ownerFullName,
                description,
                submission.releaseTime?.toStringDate()
            )
        }

        private fun successfulSubmissionModel(
            submission: ExtSubmission,
            uiUrl: String,
            stUrl: String,
            ownerFullName: String
        ): SuccessfulSubmissionModel {
            val title = submission.computedTitle?.let { "submission \"$it\"" } ?: "submission"
            return SuccessfulSubmissionModel(
                mailto = FROM,
                uiUrl = uiUrl,
                stUrl = stUrl,
                username = ownerFullName,
                userEmail = submission.owner,
                accNo = submission.accNo,
                title = title,
                releaseMessage = releaseMessage(submission, uiUrl),
                releaseDate = submission.releaseTime?.toStringDate().orEmpty(),
            )
        }

        private fun releaseMessage(sub: ExtSubmission, uiUrl: String): String {
            val releaseDate = sub.releaseTime?.toStringDate()
            val released = sub.released

            return when {
                released -> EMPTY
                releaseDate != null -> privateWithReleaseDateMessage(linkMessage(sub, uiUrl), releaseDate)
                else -> privateMessage(linkMessage(sub, uiUrl))
            }
        }

        private fun linkMessage(sub: ExtSubmission, uiUrl: String) =
            buildString {
                append("You will be able to see it only by logging in or by accessing it through this link: ")
                append("$uiUrl/studies/${sub.accNo}?key=${sub.secretKey}")
            }

        private fun privateMessage(linkMessage: String) =
            "The release date of this study is not set so it's not publicly available. $linkMessage"

        private fun privateWithReleaseDateMessage(linkMessage: String, releaseDate: String) =
            "The release date of this study is set to $releaseDate " +
                "and it will be publicly available after that. $linkMessage"
    }
}
