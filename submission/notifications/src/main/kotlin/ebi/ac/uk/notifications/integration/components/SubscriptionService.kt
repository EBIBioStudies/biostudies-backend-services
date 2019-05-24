package ebi.ac.uk.notifications.integration.components

import ebi.ac.uk.notifications.integration.model.Notification
import ebi.ac.uk.notifications.integration.model.NotificationTemplateModel
import ebi.ac.uk.notifications.integration.model.NotificationType
import io.reactivex.Observable

interface SubscriptionService {
    /**
     * Create a notification subscription (dispatched when observable is trigger) based on the given notification type.
     */
    fun <T : NotificationTemplateModel> create(subscription: NotificationType<T>, event: Observable<Notification<T>>)
}
