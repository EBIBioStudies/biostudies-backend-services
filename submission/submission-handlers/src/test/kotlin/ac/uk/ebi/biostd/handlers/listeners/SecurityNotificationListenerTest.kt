package ac.uk.ebi.biostd.handlers.listeners

import ac.uk.ebi.biostd.common.events.BIOSTUDIES_EXCHANGE
import ac.uk.ebi.biostd.handlers.config.NOTIFICATIONS_FAILED_REQUEST_ROUTING_KEY
import ebi.ac.uk.commons.http.slack.Alert
import ebi.ac.uk.commons.http.slack.NotificationsSender
import ebi.ac.uk.extended.events.SecurityNotification
import ebi.ac.uk.extended.events.SecurityNotificationType.ACTIVATION
import ebi.ac.uk.extended.events.SecurityNotificationType.ACTIVATION_BY_EMAIL
import ebi.ac.uk.extended.events.SecurityNotificationType.PASSWORD_RESET
import ebi.ac.uk.notifications.service.SecurityNotificationService
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.amqp.rabbit.core.RabbitTemplate

@ExtendWith(MockKExtension::class)
class SecurityNotificationListenerTest(
    @MockK private val rabbitTemplate: RabbitTemplate,
    @MockK private val notification: SecurityNotification,
    @MockK private val notificationsSender: NotificationsSender,
    @MockK private val notificationService: SecurityNotificationService,
) {
    private val testInstance = SecurityNotificationListener(rabbitTemplate, notificationsSender, notificationService)

    @AfterEach
    fun afterEach() = clearAllMocks()

    @BeforeEach
    fun beforeEach() {
        every { notification.email } returns "test@mail.org"
    }

    @Test
    fun `activation notification`() {
        every { notification.type } returns ACTIVATION
        every { notificationService.sendActivationNotification(notification) } answers { nothing }

        testInstance.receiveMessage(notification)

        verify(exactly = 1) { notificationService.sendActivationNotification(notification) }
        verify(exactly = 0) {
            notificationsSender.send(any())
            rabbitTemplate.convertAndSend(any())
            notificationService.sendPasswordResetNotification(any())
            notificationService.sendActivationByEmailNotification(any())
        }
    }

    @Test
    fun `activation by email notification`() {
        every { notification.type } returns ACTIVATION_BY_EMAIL
        every { notificationService.sendActivationByEmailNotification(notification) } answers { nothing }

        testInstance.receiveMessage(notification)

        verify(exactly = 1) { notificationService.sendActivationByEmailNotification(notification) }
        verify(exactly = 0) {
            notificationsSender.send(any())
            rabbitTemplate.convertAndSend(any())
            notificationService.sendActivationNotification(any())
            notificationService.sendPasswordResetNotification(any())
        }
    }

    @Test
    fun `password reset notification`() {
        every { notification.type } returns PASSWORD_RESET
        every { notificationService.sendPasswordResetNotification(notification) } answers { nothing }

        testInstance.receiveMessage(notification)

        verify(exactly = 1) { notificationService.sendPasswordResetNotification(notification) }
        verify(exactly = 0) {
            notificationsSender.send(any())
            rabbitTemplate.convertAndSend(any())
            notificationService.sendActivationNotification(any())
            notificationService.sendActivationByEmailNotification(any())
        }
    }

    @Test
    fun `failed notification`() {
        val alertSlot = slot<Alert>()
        every { notification.type } returns PASSWORD_RESET
        every { notificationsSender.send(capture(alertSlot)) } answers { nothing }
        every { notificationService.sendPasswordResetNotification(notification) } throws Exception()
        every {
            rabbitTemplate.convertAndSend(BIOSTUDIES_EXCHANGE, NOTIFICATIONS_FAILED_REQUEST_ROUTING_KEY, notification)
        } answers { nothing }

        testInstance.receiveMessage(notification)

        verify(exactly = 1) {
            notificationsSender.send(alertSlot.captured)
            notificationService.sendPasswordResetNotification(notification)
            rabbitTemplate.convertAndSend(BIOSTUDIES_EXCHANGE, NOTIFICATIONS_FAILED_REQUEST_ROUTING_KEY, notification)
        }
        verify(exactly = 0) {
            notificationService.sendActivationNotification(any())
            notificationService.sendActivationByEmailNotification(any())
        }
    }
}
