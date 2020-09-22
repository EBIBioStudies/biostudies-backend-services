package ac.uk.ebi.biostd.handlers.listeners

import ac.uk.ebi.biostd.handlers.api.BioStudiesWebConsumer
import ebi.ac.uk.extended.events.SubmissionMessage
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.ExtUser
import ebi.ac.uk.notifications.integration.NotificationProperties
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
    @MockK private val submitter: ExtUser,
    @MockK private val submission: ExtSubmission,
    @MockK private val message: SubmissionMessage,
    @MockK private val webConsumer: BioStudiesWebConsumer,
    @MockK private val rtNotificationService: RtNotificationService,
    @MockK private val notificationProperties: NotificationProperties
) {
    private val testInstance =
        SubmissionNotificationsListener(webConsumer, rtNotificationService, notificationProperties)

    @BeforeEach
    fun beforeEach() {
        mockMessage()
        mockSubmitter()
        every { notificationProperties.uiUrl } returns "ui-url"
        every { webConsumer.getExtUser("ext-user-url") } returns submitter
        every { webConsumer.getExtSubmission("ext-tab-url") } returns submission
        every { rtNotificationService.notifySubmissionRelease(submission, "Dr Owner", "ui-url") } answers { nothing }
        every { rtNotificationService.notifySuccessfulSubmission(submission, "Dr Owner", "ui-url") } answers { nothing }
    }

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun `receive submission message`() {
        testInstance.receiveSubmissionMessage(message)

        verify(exactly = 1) { webConsumer.getExtSubmission("ext-tab-url") }
        verify(exactly = 1) { rtNotificationService.notifySuccessfulSubmission(submission, "Dr Owner", "ui-url") }
    }

    @Test
    fun `receive submission message notifications disabled`() {
        every { submitter.notificationsEnabled } returns false

        testInstance.receiveSubmissionMessage(message)

        verify(exactly = 0) { webConsumer.getExtSubmission("ext-tab-url") }
        verify(exactly = 0) { rtNotificationService.notifySuccessfulSubmission(submission, "Dr Owner", "ui-url") }
    }

    @Test
    fun `receive submission release message`() {
        testInstance.receiveSubmissionReleaseMessage(message)

        verify(exactly = 1) { webConsumer.getExtSubmission("ext-tab-url") }
        verify(exactly = 1) { rtNotificationService.notifySubmissionRelease(submission, "Dr Owner", "ui-url") }
    }

    @Test
    fun `receive submission release message notifications disabled`() {
        every { submitter.notificationsEnabled } returns false

        testInstance.receiveSubmissionReleaseMessage(message)

        verify(exactly = 0) { webConsumer.getExtSubmission("ext-tab-url") }
        verify(exactly = 0) { rtNotificationService.notifySubmissionRelease(submission, "Dr Owner", "ui-url") }
    }

    private fun mockMessage() {
        every { message.extTabUrl } returns "ext-tab-url"
        every { message.extUserUrl } returns "ext-user-url"
    }

    private fun mockSubmitter() {
        every { submitter.fullName } returns "Dr Owner"
        every { submitter.notificationsEnabled } returns true
    }
}
