package ebi.ac.uk.notifications.service

import ebi.ac.uk.extended.events.SubmissionSubmitted
import ebi.ac.uk.notifications.api.RtClient
import ebi.ac.uk.notifications.integration.templates.SuccessfulSubmissionModel
import ebi.ac.uk.notifications.integration.templates.SuccessfulSubmissionTemplate
import ebi.ac.uk.notifications.persistence.service.NotificationPersistenceService
import org.apache.commons.io.IOUtils
import org.springframework.core.io.ResourceLoader
import java.nio.charset.Charset

private const val FROM = "biostudies@ebi.ac.uk"

class RtNotificationService(
    private val rtClient: RtClient,
    private val resourceLoader: ResourceLoader,
    private val notificationPersistenceService: NotificationPersistenceService
) {
    // TODO add resubmission logic
    fun notifySuccessfulSubmission(submission: SubmissionSubmitted) {
        val accNo = submission.accNo
        val subject = "BioStudies Successful Submission - $accNo"
        val content = SuccessfulSubmissionTemplate(templateContent()).getContent(toSubmissionNotification(submission))
        val ticketId = rtClient.createTicket(subject, submission.ownerEmail, content)

        notificationPersistenceService.saveRtNotification(accNo, ticketId)
    }

    private fun templateContent(): String {
        val resource = resourceLoader.getResource("classpath:templates/successful-submission.txt")
        return IOUtils.toString(resource.inputStream, Charset.defaultCharset())
    }

    private fun toSubmissionNotification(submission: SubmissionSubmitted) =
        SuccessfulSubmissionModel(
            FROM,
            submission.uiUrl,
            submission.ownerFullName,
            submission.accNo,
            submission.secretKey,
            submission.released,
            submission.title,
            submission.releaseDate)
}
