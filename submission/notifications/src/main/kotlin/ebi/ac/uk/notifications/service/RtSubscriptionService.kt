package ebi.ac.uk.notifications.service

import ebi.ac.uk.notifications.api.RtClient
import ebi.ac.uk.notifications.integration.components.SubscriptionService
import ebi.ac.uk.notifications.integration.model.Notification
import ebi.ac.uk.notifications.integration.model.NotificationTemplateModel
import ebi.ac.uk.notifications.integration.model.NotificationType
import ebi.ac.uk.notifications.persistence.service.NotificationPersistenceService
import io.reactivex.Observable

internal class RtSubscriptionService(
    private val rtClient: RtClient,
    private val notificationPersistenceService: NotificationPersistenceService
) : SubscriptionService {
    override fun <T : NotificationTemplateModel> create(
        subscription: NotificationType<T>,
        event: Observable<Notification<T>>
    ) {
        event.subscribe {
            val accNo = it.templateModel.getParams().first { param -> param.first == "ACC_NO" }.second
            val ticketId = rtClient.createTicket(subscription.template.getContent(it.templateModel))

            notificationPersistenceService.saveRtNotification(accNo, ticketId)
        }
    }
}
