package ebi.ac.uk.notifications.service

import ebi.ac.uk.extended.events.SecurityNotification
import ebi.ac.uk.extended.events.SecurityNotificationType.ACTIVATION
import ebi.ac.uk.extended.events.SecurityNotificationType.ACTIVATION_BY_EMAIL
import ebi.ac.uk.extended.events.SecurityNotificationType.PASSWORD_RESET
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

@ExtendWith(MockKExtension::class)
class SecurityNotificationServiceTest(
    @MockK private val templateLoader: TemplateLoader,
    @MockK private val simpleEmailService: SimpleEmailService
) {
    private val testInstance = SecurityNotificationService(templateLoader, simpleEmailService)

    @BeforeEach
    fun beforeEach() = clearAllMocks()

    @Test
    fun `activation notification`() {
        val activationEmail = slot<Email>()
        val notification = SecurityNotification(TEST_EMAIL, "Test User", "activation-link", ACTIVATION)

        every { templateLoader.loadTemplate("activation.html") } returns "activation"
        every { simpleEmailService.send(capture(activationEmail)) } answers { nothing }

        testInstance.sendActivationNotification(notification)

        val email = activationEmail.captured
        verify(exactly = 1) { simpleEmailService.send(email) }
        assertEmail(email, "BioStudies Account Activation", "activation")
    }

    @Test
    fun `activation by email notification`() {
        val activationEmail = slot<Email>()
        val notification = SecurityNotification(TEST_EMAIL, "Test User", "activation-link", ACTIVATION_BY_EMAIL)

        every { templateLoader.loadTemplate("activation-by-email.html") } returns "activation"
        every { simpleEmailService.send(capture(activationEmail)) } answers { nothing }

        testInstance.sendActivationByEmailNotification(notification)

        val email = activationEmail.captured
        verify(exactly = 1) { simpleEmailService.send(email) }
        assertEmail(email, "BioStudies Account Password Setup", "activation")
    }

    @Test
    fun `password reset notification`() {
        val resetEmail = slot<Email>()
        val notification = SecurityNotification(TEST_EMAIL, "Test User", "password-reset-link", PASSWORD_RESET)

        every { templateLoader.loadTemplate("reset-password.html") } returns "reset password"
        every { simpleEmailService.send(capture(resetEmail)) } answers { nothing }

        testInstance.sendPasswordResetNotification(notification)

        val email = resetEmail.captured
        verify(exactly = 1) { simpleEmailService.send(email) }
        assertEmail(email, "BioStudies Account Password Reset", "reset password")
    }

    private fun assertEmail(email: Email, subject: String, content: String) {
        assertThat(email.from).isEqualTo(EMAIL_FROM)
        assertThat(email.to).isEqualTo(TEST_EMAIL)
        assertThat(email.subject).isEqualTo(subject)
        assertThat(email.content).isEqualTo(content)
    }
}
