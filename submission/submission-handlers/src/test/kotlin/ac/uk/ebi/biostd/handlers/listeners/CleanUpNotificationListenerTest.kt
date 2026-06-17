package ac.uk.ebi.biostd.handlers.listeners

import ac.uk.ebi.biostd.persistence.common.service.NotificationErrorDataService
import ebi.ac.uk.extended.events.CleanUpNotification
import ebi.ac.uk.notifications.service.CleanUpNotificationService
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.amqp.rabbit.core.RabbitTemplate

@ExtendWith(MockKExtension::class)
class CleanUpNotificationListenerTest(
    @param:MockK private val rabbitTemplate: RabbitTemplate,
    @param:MockK private val notificationsSender: ebi.ac.uk.commons.http.slack.NotificationsSender,
    @param:MockK private val notificationService: CleanUpNotificationService,
    @param:MockK private val notificationErrorService: NotificationErrorDataService,
) {
    private val testInstance =
        CleanUpNotificationListener(rabbitTemplate, notificationsSender, notificationService, notificationErrorService)

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun `cleanup notification`() {
        val notification =
            CleanUpNotification(
                email = "test@ebi.ac.uk",
                username = "Test User",
                lastActivityDate = "2026-06-08",
                cleanUpDate = "2026-08-08",
                emailSubject = "Inactivity notice - Cleanup of your BioStudies workspace",
                emailTemplate = "clean-up-warning",
            )

        every { notificationService.sendCleanUpNotification(notification) } answers { nothing }

        testInstance.receiveMessage(notification)

        verify(exactly = 1) { notificationService.sendCleanUpNotification(notification) }
    }
}
