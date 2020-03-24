package ac.uk.ebi.biostd.notifications

import ac.uk.ebi.biostd.notifications.templates.SuccessfulSubmissionModel
import ac.uk.ebi.biostd.notifications.templates.SuccessfulSubmissionTemplate
import ac.uk.ebi.biostd.submission.events.SuccessfulSubmission
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
        val resource = resourceLoader.getResource("classpath:emails/successful-submission.html")
        val content = IOUtils.toString(resource.inputStream, Charset.defaultCharset())
        val subscription =
            NotificationType(EMAIL_FROM, "BioStudies Successful Submission", SuccessfulSubmissionTemplate(content))

        rtSubscriptionService.create(subscription, successfulSubmission.map { asSubmissionNotification(it) })
    }

    private fun asSubmissionNotification(source: SuccessfulSubmission) =
        Notification(
            source.user.email,
            SuccessfulSubmissionModel(
                FROM,
                source.user.email,
                source.user.fullName!!,
                source.accNo,
                source.user.secretKey,
                source.released,
                source.title,
                source.releaseDate
            ))
}
