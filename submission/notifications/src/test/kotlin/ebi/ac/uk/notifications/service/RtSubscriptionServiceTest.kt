package ebi.ac.uk.notifications.service

import ebi.ac.uk.notifications.api.RtClient
import ebi.ac.uk.notifications.integration.model.Notification
import ebi.ac.uk.notifications.integration.model.NotificationTemplate
import ebi.ac.uk.notifications.integration.model.NotificationTemplateModel
import ebi.ac.uk.notifications.integration.model.NotificationType
import ebi.ac.uk.notifications.persistence.model.SubmissionRt
import ebi.ac.uk.notifications.persistence.service.NotificationPersistenceService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Observable
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RtSubscriptionServiceTest {
    private val rtClient = mockk<RtClient>()
    private val notificationPersistenceService = mockk<NotificationPersistenceService>()
    private val testInstance = RtSubscriptionService(rtClient, notificationPersistenceService)
    private val template = mockk<NotificationTemplateModel>()
    private val notification = mockk<Notification<NotificationTemplateModel>>()
    private val subscription = mockk<NotificationType<NotificationTemplateModel>>()
    private val subscriptionTemplate = mockk<NotificationTemplate<NotificationTemplateModel>>()

    @BeforeEach
    fun beforeEach() {
        every { subscription.subject } returns "Test"
        every { notification.receiver } returns "test@mail.org"
        every { notification.templateModel } returns template
        every { subscription.template } returns subscriptionTemplate
        every { template.getParams() } returns listOf("ACC_NO" to "S-TEST123")
        every { subscriptionTemplate.getContent(template) } returns "Test Content"
        every { rtClient.createTicket("Test", "test@mail.org", "Test Content") } returns "8521"
        every {
            notificationPersistenceService.saveRtNotification("S-TEST123", "8521")
        } returns SubmissionRt("S-TEST123", "8521")
    }

    @Test
    fun create() {
        val event = Observable.just(notification)

        testInstance.create(subscription, event)
        event.doOnNext { }

        verify(exactly = 1) {
            rtClient.createTicket("Test", "test@mail.org", "Test Content")
            notificationPersistenceService.saveRtNotification("S-TEST123", "8521")
        }
    }
}
