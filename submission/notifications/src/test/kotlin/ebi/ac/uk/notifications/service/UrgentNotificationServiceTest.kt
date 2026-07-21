package ebi.ac.uk.notifications.service

import ac.uk.ebi.biostd.common.properties.NotificationProperties
import ebi.ac.uk.extended.events.UrgentNotification
import ebi.ac.uk.notifications.model.Email
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class UrgentNotificationServiceTest(
    @param:MockK private val properties: NotificationProperties,
    @param:MockK private val simpleEmailService: SimpleEmailService,
) {
    private val testInstance = UrgentNotificationService(properties, simpleEmailService)

    @Test
    fun `urgent notification`() {
        val errorEmail = slot<Email>()
        val notification = UrgentNotification("TEST ERROR", "problem details")

        every { properties.slackUrgentChannelEmail } returns "urgent@mail.com"
        every { simpleEmailService.send(capture(errorEmail)) } answers { nothing }

        testInstance.sendUrgentNotification(notification)

        val email = errorEmail.captured
        verify(exactly = 1) { simpleEmailService.send(email) }
        assertThat(email.from).isEqualTo(EMAIL_FROM)
        assertThat(email.to).isEqualTo("urgent@mail.com")
        assertThat(email.subject).isEqualTo("TEST ERROR")
        assertThat(email.content).isEqualTo("problem details")
    }
}
