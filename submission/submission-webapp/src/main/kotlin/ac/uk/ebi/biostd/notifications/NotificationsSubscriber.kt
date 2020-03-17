package ac.uk.ebi.biostd.notifications

import ac.uk.ebi.biostd.notifications.templates.ActivationModel
import ac.uk.ebi.biostd.notifications.templates.ActivationTemplate
import ac.uk.ebi.biostd.notifications.templates.PasswordResetModel
import ac.uk.ebi.biostd.notifications.templates.PasswordResetTemplate
import ac.uk.ebi.biostd.notifications.templates.SuccessfulSubmissionModel
import ac.uk.ebi.biostd.notifications.templates.SuccessfulSubmissionTemplate
import ac.uk.ebi.biostd.submission.events.SuccessfulSubmission
import ebi.ac.uk.notifications.integration.components.SubscriptionService
import ebi.ac.uk.notifications.integration.model.Notification
import ebi.ac.uk.notifications.integration.model.NotificationType
import ebi.ac.uk.security.integration.model.events.PasswordReset
import ebi.ac.uk.security.integration.model.events.UserRegister
import io.reactivex.Observable
import org.apache.commons.io.IOUtils
import org.springframework.core.io.ResourceLoader
import java.nio.charset.Charset
import javax.annotation.PostConstruct

private const val FROM = "biostudies@ebi.ac.uk"
private const val EMAIL_FROM = "BioStudy <biostudies@ebi.ac.uk>"

internal class NotificationsSubscriber(
    private val subscriptionService: SubscriptionService,
    private val resourceLoader: ResourceLoader,
    private val userPreRegister: Observable<UserRegister>,
    private val passwordReset: Observable<PasswordReset>,
    private val successfulSubmission: Observable<SuccessfulSubmission>
) {
    @PostConstruct
    fun activationSubscription() {
        val template = ActivationTemplate(getTemplateContent("activation.html"))
        val subscription = NotificationType(EMAIL_FROM, "BioStudies account activation", template)
        subscriptionService.create(subscription, userPreRegister.map { asActivationNotification(it) })
    }

    @PostConstruct
    fun passwordReset() {
        val template = PasswordResetTemplate(getTemplateContent("reset-password.html"))
        val subscription = NotificationType(EMAIL_FROM, "BioStudies password reset", template)
        subscriptionService.create(subscription, passwordReset.map { asPasswordResetNotification(it) })
    }

    @PostConstruct
    fun successfulSubmission() {
        val template = SuccessfulSubmissionTemplate(getTemplateContent("successful-submission.html"))
        val subscription = NotificationType(EMAIL_FROM, "BioStudies Successful Submission", template)
        subscriptionService.create(subscription, successfulSubmission.map { asSuccessfulSubmissionNotification(it) })
    }

    private fun asActivationNotification(source: UserRegister) =
        Notification(source.user.email, ActivationModel(FROM, source.activationLink, source.user.fullName))

    private fun asPasswordResetNotification(source: PasswordReset) =
        Notification(source.user.email, PasswordResetModel(FROM, source.activationLink, source.user.fullName))

    private fun asSuccessfulSubmissionNotification(source: SuccessfulSubmission) =
        Notification(
            source.user.email,
            SuccessfulSubmissionModel(
                FROM,
                source.user.fullName!!,
                source.accNo,
                source.user.secretKey,
                source.released,
                source.title,
                source.releaseDate
            ))

    private fun getTemplateContent(templateName: String): String {
        val resource = resourceLoader.getResource("classpath:emails/$templateName")
        return IOUtils.toString(resource.inputStream, Charset.defaultCharset())
    }
}
