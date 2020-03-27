package ac.uk.ebi.biostd.notifications

import ac.uk.ebi.biostd.notifications.templates.SuccessfulSubmissionModel
import ac.uk.ebi.biostd.notifications.templates.SuccessfulSubmissionTemplate
import ac.uk.ebi.biostd.submission.events.SuccessfulSubmission
import ebi.ac.uk.extended.model.releaseDate
import ebi.ac.uk.notifications.integration.components.SubscriptionService
import ebi.ac.uk.notifications.integration.model.Notification
import ebi.ac.uk.notifications.integration.model.NotificationType
import io.reactivex.Observable
import org.apache.commons.io.IOUtils
import org.springframework.core.io.ResourceLoader
import java.nio.charset.Charset
import javax.annotation.PostConstruct

private const val FROM = "biostudies@ebi.ac.uk"
private const val EMAIL_FROM = "BioStudy <biostudies@ebi.ac.uk>"

internal class RtNotificationSubscriber(
    private val resourceLoader: ResourceLoader,
    private val rtSubscriptionService: SubscriptionService,
    private val successfulSubmission: Observable<SuccessfulSubmission>
) {
    @PostConstruct
    fun successfulSubmission() {
        val resource = resourceLoader.getResource("classpath:emails/successful-submission.txt")
        val templateContent = IOUtils.toString(resource.inputStream, Charset.defaultCharset())
        val subs = NotificationType(
            EMAIL_FROM, "BioStudies Successful Submission", SuccessfulSubmissionTemplate(templateContent))

        rtSubscriptionService.create(subs, successfulSubmission.map { asSubmissionNotification(it) })
    }


    // TODO test all scenarios
    // TODO fix the nullable fullname
    private fun asSubmissionNotification(source: SuccessfulSubmission) =
        Notification(
            source.user.email,
            SuccessfulSubmissionModel(
                FROM,
                source.user.fullName!!,
                source.submission.accNo,
                source.user.secretKey,
                source.submission.released,
                source.submission.title ?: "",
                source.submission.releaseDate
            ))
}
