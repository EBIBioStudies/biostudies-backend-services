package ac.uk.ebi.biostd.notifications

import ac.uk.ebi.biostd.notifications.templates.ActivationModel
import ac.uk.ebi.biostd.notifications.templates.ActivationTemplate
import ac.uk.ebi.biostd.notifications.templates.PasswordResetModel
import ac.uk.ebi.biostd.notifications.templates.PasswordResetTemplate
import ebi.ac.uk.notifications.integration.components.SubscriptionService
import ebi.ac.uk.notifications.integration.model.Notification
import ebi.ac.uk.notifications.integration.model.NotificationType
import ebi.ac.uk.security.integration.model.events.PasswordReset
import ebi.ac.uk.security.integration.model.events.UserPreRegister
import io.reactivex.Observable
import org.springframework.core.io.ResourceLoader
import org.springframework.util.FileCopyUtils
import javax.annotation.PostConstruct

private const val FROM = "biostudies@ebi.ac.uk"
private const val EMAIL_FROM = "BioStudy <biostudies@ebi.ac.uk>"

internal class NotificationsSubscriber(
    private val subscriptionService: SubscriptionService,
    private val resourceLoader: ResourceLoader,
    private val userPreRegister: Observable<UserPreRegister>,
    private val passwordReset: Observable<PasswordReset>
) {

    @PostConstruct
    fun activationSubscription() {
        val template = ActivationTemplate(getTemplateContent("activation.html"))
        val subscription = NotificationType(EMAIL_FROM, "BioStudies account activation", template)
        subscriptionService.create(subscription, userPreRegister.map { asNotification(it) })
    }

    private fun asNotification(source: UserPreRegister) =
        Notification(source.user.email, ActivationModel(FROM, source.activationLink, source.user.fullName))

    @PostConstruct
    fun passwordReset() {
        val template = PasswordResetTemplate(getTemplateContent("reset-password.html"))
        val subscription = NotificationType(EMAIL_FROM, "BioStudies password reset", template)
        subscriptionService.create(subscription, passwordReset.map { asNotification(it) })
    }

    private fun asNotification(source: PasswordReset) =
        Notification(source.user.email, PasswordResetModel(FROM, source.activationLink, source.user.fullName))

    private fun getTemplateContent(templateName: String): String {
        val resource = resourceLoader.getResource("classpath:emails/$templateName")
        return FileCopyUtils.copyToString(resource.file.reader())
    }
}
