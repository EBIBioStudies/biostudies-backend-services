package ebi.ac.uk.notifications.service

import ac.uk.ebi.biostd.persistence.common.service.NotificationsDataService
import ebi.ac.uk.base.trim
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.computedTitle
import ebi.ac.uk.notifications.api.RtClient
import ebi.ac.uk.notifications.integration.templates.SubmissionReleaseModel
import ebi.ac.uk.notifications.integration.templates.SubmissionReleaseTemplate
import ebi.ac.uk.notifications.integration.templates.SuccessfulSubmissionModel
import ebi.ac.uk.notifications.integration.templates.SuccessfulSubmissionTemplate
import ebi.ac.uk.notifications.util.TemplateLoader
import ebi.ac.uk.util.date.toStringDate

internal const val FROM = "biostudies@ebi.ac.uk"
internal const val SUBMISSION_RELEASE_TEMPLATE = "release-notification.txt"
internal const val SUCCESSFUL_SUBMISSION_TEMPLATE = "successful-submission.txt"
internal const val SUCCESSFUL_RESUBMISSION_TEMPLATE = "successful-resubmission.txt"

class RtNotificationService(
    private val rtClient: RtClient,
    private val templateLoader: TemplateLoader,
    private val notificationsDataService: NotificationsDataService
) {
    fun notifySuccessfulSubmission(submission: ExtSubmission, ownerFullName: String, uiUrl: String) {
        val accNo = submission.accNo
        val subject = "BioStudies Submission - $accNo"
        val notification = submissionNotification(submission, ownerFullName, uiUrl)
        val content = SuccessfulSubmissionTemplate(submissionTemplateContent(submission))
            .getContent(notification).trim()

        createOrUpdateTicket(accNo, subject, submission.owner, content)
    }

    fun notifySubmissionRelease(submission: ExtSubmission, ownerFullName: String, uiUrl: String) {
        val accNo = submission.accNo
        val subject = "BioStudies Submission - $accNo"
        val notification = releaseNotification(submission, ownerFullName, uiUrl)
        val content = SubmissionReleaseTemplate(templateLoader.loadTemplate(SUBMISSION_RELEASE_TEMPLATE))
            .getContent(notification).trim()

        createOrUpdateTicket(accNo, subject, submission.owner, content)
    }

    private fun createOrUpdateTicket(accNo: String, subject: String, owner: String, content: String) =
        when (val ticketId = notificationsDataService.findTicketId(accNo)) {
            null -> createTicket(accNo, subject, owner, content)
            else -> rtClient.commentTicket(ticketId, content)
        }

    private fun createTicket(accNo: String, subject: String, owner: String, content: String) {
        val ticketId = rtClient.createTicket(accNo, subject, owner, content)
        notificationsDataService.saveRtNotification(accNo, ticketId)
    }

    private fun submissionTemplateContent(submission: ExtSubmission): String {
        val template = when (submission.version) {
            1 -> SUCCESSFUL_SUBMISSION_TEMPLATE
            else -> SUCCESSFUL_RESUBMISSION_TEMPLATE
        }

        return templateLoader.loadTemplate(template)
    }

    private fun releaseNotification(submission: ExtSubmission, ownerFullName: String, uiUrl: String) =
        SubmissionReleaseModel(
            FROM,
            uiUrl,
            ownerFullName,
            submission.accNo,
            submission.computedTitle?.let { " - \"$it\"" },
            submission.releaseTime?.toStringDate()
        )

    private fun submissionNotification(submission: ExtSubmission, ownerFullName: String, uiUrl: String) =
        SuccessfulSubmissionModel(
            FROM,
            uiUrl,
            ownerFullName,
            submission.accNo,
            submission.secretKey,
            submission.released,
            submission.computedTitle?.let { "\"$it\"" },
            submission.releaseTime?.toStringDate()
        )
}
