package ac.uk.ebi.biostd.handlers.listeners

import ac.uk.ebi.biostd.common.events.BIOSTUDIES_EXCHANGE
import ac.uk.ebi.biostd.handlers.config.NOTIFICATIONS_FAILED_REQUEST_ROUTING_KEY
import ebi.ac.uk.notifications.service.SecurityNotificationService
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.amqp.rabbit.core.RabbitTemplate
import uk.ac.ebi.biostd.commons.http.slack.Alert
import uk.ac.ebi.biostd.commons.http.slack.NotificationsSender
import uk.ac.ebi.biostd.extended.events.SecurityNotification
import uk.ac.ebi.biostd.extended.events.SecurityNotificationType.ACTIVATION
import uk.ac.ebi.biostd.extended.events.SecurityNotificationType.ACTIVATION_BY_EMAIL
import uk.ac.ebi.biostd.extended.events.SecurityNotificationType.PASSWORD_RESET

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
    fun `activation notification`() =
        runTest {
            every { notification.type } returns ACTIVATION
            every { notificationService.sendActivationNotification(notification) } answers { nothing }

            testInstance.receiveMessage(notification)

            verify(exactly = 1) { notificationService.sendActivationNotification(notification) }
            coVerify(exactly = 0) {
                notificationsSender.send(any())
                rabbitTemplate.convertAndSend(any())
                notificationService.sendPasswordResetNotification(any())
                notificationService.sendActivationByEmailNotification(any())
            }
        }

    @Test
    fun `activation by email notification`() =
        runTest {
            every { notification.type } returns ACTIVATION_BY_EMAIL
            every { notificationService.sendActivationByEmailNotification(notification) } answers { nothing }

            testInstance.receiveMessage(notification)

            verify(exactly = 1) { notificationService.sendActivationByEmailNotification(notification) }
            coVerify(exactly = 0) {
                notificationsSender.send(any())
                rabbitTemplate.convertAndSend(any())
                notificationService.sendActivationNotification(any())
                notificationService.sendPasswordResetNotification(any())
            }
        }

    @Test
    fun `password reset notification`() =
        runTest {
            every { notification.type } returns PASSWORD_RESET
            every { notificationService.sendPasswordResetNotification(notification) } answers { nothing }

            testInstance.receiveMessage(notification)

            verify(exactly = 1) { notificationService.sendPasswordResetNotification(notification) }
            coVerify(exactly = 0) {
                notificationsSender.send(any())
                rabbitTemplate.convertAndSend(any())
                notificationService.sendActivationNotification(any())
                notificationService.sendActivationByEmailNotification(any())
            }
        }

    @Test
    fun `failed notification`() =
        runTest {
            val alertSlot = slot<Alert>()
            every { notification.type } returns PASSWORD_RESET
            coEvery { notificationsSender.send(capture(alertSlot)) } answers { nothing }
            every { notificationService.sendPasswordResetNotification(notification) } throws Exception()
            every {
                rabbitTemplate.convertAndSend(BIOSTUDIES_EXCHANGE, NOTIFICATIONS_FAILED_REQUEST_ROUTING_KEY, notification)
            } answers { nothing }

            testInstance.receiveMessage(notification)

            coVerify(exactly = 1) {
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
