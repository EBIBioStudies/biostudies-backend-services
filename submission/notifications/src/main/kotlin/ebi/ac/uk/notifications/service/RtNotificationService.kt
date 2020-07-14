package ebi.ac.uk.notifications.service

import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.notifications.api.RtClient
import ebi.ac.uk.notifications.integration.templates.SuccessfulSubmissionModel
import ebi.ac.uk.notifications.integration.templates.SuccessfulSubmissionTemplate
import ebi.ac.uk.notifications.persistence.service.NotificationPersistenceService
import ebi.ac.uk.util.date.toStringDate
import org.apache.commons.io.IOUtils
import org.springframework.core.io.ResourceLoader
import java.nio.charset.Charset

internal const val FROM = "biostudies@ebi.ac.uk"
internal const val SUCCESSFUL_SUBMISSION_TEMPLATE = "successful-submission.txt"
internal const val SUCCESSFUL_RESUBMISSION_TEMPLATE = "successful-resubmission.txt"

class RtNotificationService(
    private val rtClient: RtClient,
    private val resourceLoader: ResourceLoader,
    private val notificationPersistenceService: NotificationPersistenceService
) {
    fun notifySuccessfulSubmission(submission: ExtSubmission, ownerFullName: String, uiUrl: String) {
        val accNo = submission.accNo
        val subject = "BioStudies Successful Submission - $accNo"
        val notification = submissionNotification(submission, ownerFullName, uiUrl)
        val content = SuccessfulSubmissionTemplate(templateContent(submission)).getContent(notification)

        when (val ticketId = notificationPersistenceService.findTicketId(accNo)) {
            null -> createTicket(accNo, subject, submission.owner, content)
            else -> rtClient.commentTicket(ticketId, content)
        }
    }

    private fun createTicket(accNo: String, subject: String, owner: String, content: String) {
        val ticketId = rtClient.createTicket(subject, owner, content)
        notificationPersistenceService.saveRtNotification(accNo, ticketId)
    }

    private fun templateContent(submission: ExtSubmission): String {
        val template = when (submission.version) {
            1 -> SUCCESSFUL_SUBMISSION_TEMPLATE
            else -> SUCCESSFUL_RESUBMISSION_TEMPLATE
        }

        val resource = resourceLoader.getResource("classpath:templates/$template")
        return IOUtils.toString(resource.inputStream, Charset.defaultCharset())
    }

    private fun submissionNotification(submission: ExtSubmission, ownerFullName: String, uiUrl: String) =
        SuccessfulSubmissionModel(
            FROM,
            uiUrl,
            ownerFullName,
            submission.accNo,
            submission.secretKey,
            submission.released,
            submission.title ?: "",
            submission.releaseTime?.toStringDate() ?: "")
}
