package ebi.ac.uk.notifications.service

import ac.uk.ebi.biostd.common.properties.NotificationProperties
import ebi.ac.uk.extended.events.CleanUpNotification
import ebi.ac.uk.notifications.model.Email
import ebi.ac.uk.notifications.util.TemplateLoader
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

private const val TEST_EMAIL = "test@ebi.ac.uk"
private const val BBC_EMAIL = "test-2@ebi.ac.uk"

@ExtendWith(MockKExtension::class)
class CleanUpNotificationServiceTest(
    @MockK private val templateLoader: TemplateLoader,
    @MockK private val simpleEmailService: SimpleEmailService,
    @MockK private val properties: NotificationProperties,
) {
    private val testInstance = CleanUpNotificationService(templateLoader, simpleEmailService, properties)

    @BeforeEach
    fun beforeEach() = clearAllMocks()

    @Test
    fun `cleanup notification`() {
        val cleanupEmail = slot<Email>()
        val notification =
            CleanUpNotification(
                email = TEST_EMAIL,
                username = "Test User",
                lastActivityDate = "2026-06-08",
                cleanUpDate = "2026-08-08",
                emailSubject = "Inactivity notice - Cleanup of your BioStudies workspace",
                emailTemplate = "clean-up-warning",
            )

        every { properties.bccEmail } returns BBC_EMAIL
        every { templateLoader.loadTemplate("cleanup/clean-up-warning.html") } returns
            "<p>\${USERNAME} \${LAST_ACTIVITY_DATE} \${CLEAN_UP_DATE}</p>"
        every { simpleEmailService.send(capture(cleanupEmail)) } answers { nothing }

        testInstance.sendCleanUpNotification(notification)

        val email = cleanupEmail.captured
        verify(exactly = 1) { simpleEmailService.send(email) }
        assertEmail(email, "Inactivity notice - Cleanup of your BioStudies workspace", "<p>Test User 2026-06-08 2026-08-08</p>")
    }

    private fun assertEmail(
        email: Email,
        subject: String,
        content: String,
    ) {
        assertThat(email.from).isEqualTo(EMAIL_FROM)
        assertThat(email.to).isEqualTo(TEST_EMAIL)
        assertThat(email.subject).isEqualTo(subject)
        assertThat(email.content).isEqualTo(content)
    }
}
