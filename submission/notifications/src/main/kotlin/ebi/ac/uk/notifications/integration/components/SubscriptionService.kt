package ebi.ac.uk.notifications.integration.components

import ebi.ac.uk.notifications.integration.model.Notification
import ebi.ac.uk.notifications.integration.model.NotificationTemplateModel
import ebi.ac.uk.notifications.integration.model.NotificationType
import io.reactivex.Observable

interface SubscriptionService {

    fun <T : NotificationTemplateModel> create(subscription: NotificationType<T>, event: Observable<Notification<T>>)
}
