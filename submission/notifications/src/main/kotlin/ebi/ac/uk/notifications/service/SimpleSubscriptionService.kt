package ebi.ac.uk.notifications.service

import ebi.ac.uk.notifications.integration.components.SubscriptionService
import ebi.ac.uk.notifications.integration.model.Notification
import ebi.ac.uk.notifications.integration.model.NotificationTemplateModel
import ebi.ac.uk.notifications.integration.model.NotificationType
import ebi.ac.uk.notifications.model.Email
import io.reactivex.Observable

internal class SimpleSubscriptionService(private val emailService: SimpleEmailService) : SubscriptionService {

    /**
     * Create a subscription based on the given notification and Observable.
     */
    override fun <T : NotificationTemplateModel> create(
        subscription: NotificationType<T>,
        event: Observable<Notification<T>>
    ) {
        event.subscribe {
            emailService.send(Email(
                from = subscription.from,
                to = it.receiver,
                subject = subscription.subject,
                content = subscription.template.getContent(it.templateModel)))
        }
    }
}
