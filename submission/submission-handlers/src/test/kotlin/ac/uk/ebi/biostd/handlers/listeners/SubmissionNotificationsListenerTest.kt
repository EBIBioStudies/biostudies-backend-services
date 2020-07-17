package ac.uk.ebi.biostd.handlers.listeners

import ac.uk.ebi.biostd.handlers.api.BioStudiesWebConsumer
import ebi.ac.uk.extended.events.SubmissionSubmitted
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.notifications.service.RtNotificationService
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class SubmissionNotificationsListenerTest(
    @MockK private val submission: ExtSubmission,
    @MockK private val message: SubmissionSubmitted,
    @MockK private val webConsumer: BioStudiesWebConsumer,
    @MockK private val rtNotificationService: RtNotificationService
) {
    private val testInstance = SubmissionNotificationsListener(webConsumer, rtNotificationService)

    @BeforeEach
    fun beforeEach() {
        mockMessage()
        every { webConsumer.getExtSubmission("ext-tab-url") } returns submission
        every { rtNotificationService.notifySuccessfulSubmission(submission, "Dr Owner", "ui-url") } answers { nothing }
    }

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun `receive message`() {
        testInstance.receiveMessage(message)

        verify(exactly = 1) { webConsumer.getExtSubmission("ext-tab-url") }
        verify(exactly = 1) { rtNotificationService.notifySuccessfulSubmission(submission, "Dr Owner", "ui-url") }
    }

    @Test
    fun `notifications disabled`() {
        every { message.notificationsEnabled } returns false

        testInstance.receiveMessage(message)

        verify(exactly = 0) { webConsumer.getExtSubmission("ext-tab-url") }
        verify(exactly = 0) { rtNotificationService.notifySuccessfulSubmission(submission, "Dr Owner", "ui-url") }
    }

    private fun mockMessage() {
        every { message.uiUrl } returns "ui-url"
        every { message.extTabUrl } returns "ext-tab-url"
        every { message.ownerFullName } returns "Dr Owner"
        every { message.notificationsEnabled } returns true
    }
}
