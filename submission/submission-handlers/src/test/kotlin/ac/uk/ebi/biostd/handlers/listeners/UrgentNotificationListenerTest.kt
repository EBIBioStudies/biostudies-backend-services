package ac.uk.ebi.biostd.handlers.listeners

import ac.uk.ebi.biostd.common.events.BIOSTUDIES_EXCHANGE
import ac.uk.ebi.biostd.handlers.config.NOTIFICATIONS_FAILED_REQUEST_ROUTING_KEY
import ebi.ac.uk.commons.http.slack.Alert
import ebi.ac.uk.commons.http.slack.NotificationsSender
import ebi.ac.uk.extended.events.UrgentNotification
import ebi.ac.uk.notifications.service.UrgentNotificationService
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

@ExtendWith(MockKExtension::class)
class UrgentNotificationListenerTest(
    @param:MockK private val rabbitTemplate: RabbitTemplate,
    @param:MockK private val notification: UrgentNotification,
    @param:MockK private val notificationsSender: NotificationsSender,
    @param:MockK private val notificationService: UrgentNotificationService,
) {
    private val testInstance = UrgentNotificationListener(rabbitTemplate, notificationsSender, notificationService)

    @AfterEach
    fun afterEach() = clearAllMocks()

    @BeforeEach
    fun beforeEach() {
        every { notification.content } returns "test-error-type"
        every { notification.subject } returns "test-error-message"
    }

    @Test
    fun `urgent notification`() =
        runTest {
            every { notificationService.sendUrgentNotification(notification) } answers { nothing }

            testInstance.receiveMessage(notification)

            verify(exactly = 1) { notificationService.sendUrgentNotification(notification) }
            coVerify(exactly = 0) {
                notificationsSender.send(any())
                rabbitTemplate.convertAndSend(any<String>(), any<String>(), any<UrgentNotification>())
            }
        }

    @Test
    fun `failed urgent notification`() =
        runTest {
            val alertSlot = slot<Alert>()
            coEvery { notificationsSender.send(capture(alertSlot)) } answers { nothing }
            every { notificationService.sendUrgentNotification(notification) } throws Exception()
            every {
                rabbitTemplate.convertAndSend(BIOSTUDIES_EXCHANGE, NOTIFICATIONS_FAILED_REQUEST_ROUTING_KEY, notification)
            } answers { nothing }

            testInstance.receiveMessage(notification)

            coVerify(exactly = 1) {
                notificationsSender.send(alertSlot.captured)
                notificationService.sendUrgentNotification(notification)
                rabbitTemplate.convertAndSend(BIOSTUDIES_EXCHANGE, NOTIFICATIONS_FAILED_REQUEST_ROUTING_KEY, notification)
            }
        }
}
