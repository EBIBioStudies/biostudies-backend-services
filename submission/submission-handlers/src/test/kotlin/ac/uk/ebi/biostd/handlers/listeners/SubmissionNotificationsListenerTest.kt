package ac.uk.ebi.biostd.handlers.listeners

import ac.uk.ebi.biostd.common.events.BIOSTUDIES_EXCHANGE
import ac.uk.ebi.biostd.common.properties.NotificationProperties
import ac.uk.ebi.biostd.handlers.api.BioStudiesWebConsumer
import ac.uk.ebi.biostd.handlers.config.NOTIFICATIONS_FAILED_REQUEST_ROUTING_KEY
import ebi.ac.uk.commons.http.slack.NotificationsSender
import ebi.ac.uk.commons.http.slack.SystemNotification
import ebi.ac.uk.extended.events.FailedRequestMessage
import ebi.ac.uk.extended.events.SubmissionMessage
import ebi.ac.uk.extended.model.ExtCollection
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.ExtUser
import ebi.ac.uk.notifications.service.RtNotificationService
import io.mockk.called
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
class SubmissionNotificationsListenerTest(
    @MockK private val submitter: ExtUser,
    @MockK private val submission: ExtSubmission,
    @MockK private val message: SubmissionMessage,
    @MockK private val rabbitTemplate: RabbitTemplate,
    @MockK private val webConsumer: BioStudiesWebConsumer,
    @MockK private val notificationsSender: NotificationsSender,
    @MockK private val rtNotificationService: RtNotificationService,
    @MockK private val notificationProperties: NotificationProperties,
) {
    private val testInstance =
        SubmissionNotificationsListener(
            rabbitTemplate,
            webConsumer,
            notificationsSender,
            rtNotificationService,
            notificationProperties
        )

    @BeforeEach
    fun beforeEach() {
        mockRabbit()
        mockMessage()
        mockSubmitter()
        every { submission.collections } returns emptyList()
        every { notificationProperties.uiUrl } returns "ui-url"
        every { notificationProperties.stUrl } returns "st-url"
        every { webConsumer.getExtUser("ext-user-url") } returns submitter
        every { webConsumer.getExtSubmission("ext-tab-url") } returns submission
        every {
            rtNotificationService.notifySubmissionRelease(submission, "Dr Owner", "ui-url", "st-url")
        } answers { nothing }
        every {
            rtNotificationService.notifySuccessfulSubmission(submission, "Dr Owner", "ui-url", "st-url")
        } answers { nothing }
    }

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun `receive submission message`() {
        testInstance.receiveSubmissionMessage(message)

        verify { rabbitTemplate wasNot called }
        verify(exactly = 1) {
            webConsumer.getExtSubmission("ext-tab-url")
            rtNotificationService.notifySuccessfulSubmission(submission, "Dr Owner", "ui-url", "st-url")
        }
    }

    @Test
    fun `receive submission message with collection`() {
        every { submission.collections } returns listOf(ExtCollection("BioImages"), ExtCollection("BioImages-EMPIAR"))
        every {
            rtNotificationService.notifySuccessfulSubmission(submission, "Dr Owner", "ui-url/bioimages", "st-url")
        } answers { nothing }

        testInstance.receiveSubmissionMessage(message)

        verify { rabbitTemplate wasNot called }
        verify(exactly = 1) {
            webConsumer.getExtSubmission("ext-tab-url")
            rtNotificationService.notifySuccessfulSubmission(submission, "Dr Owner", "ui-url/bioimages", "st-url")
        }
    }

    @Test
    fun `receive submission message notifications disabled`() {
        every { submitter.notificationsEnabled } returns false

        testInstance.receiveSubmissionMessage(message)

        verify(exactly = 0) { webConsumer.getExtSubmission(any()) }
        verify {
            rabbitTemplate wasNot called
            rtNotificationService wasNot called
        }
    }

    @Test
    fun `receive submission release message`() {
        testInstance.receiveSubmissionReleaseMessage(message)

        verify { rabbitTemplate wasNot called }
        verify(exactly = 1) {
            webConsumer.getExtSubmission("ext-tab-url")
            rtNotificationService.notifySubmissionRelease(submission, "Dr Owner", "ui-url", "st-url")
        }
    }

    @Test
    fun `receive submission release message notifications disabled`() {
        every { submitter.notificationsEnabled } returns false

        testInstance.receiveSubmissionReleaseMessage(message)

        verify(exactly = 0) { webConsumer.getExtSubmission(any()) }
        verify {
            rabbitTemplate wasNot called
            rtNotificationService wasNot called
        }
    }

    @Test
    fun `notify failed submission`() {
        val notificationSlot = slot<SystemNotification>()
        val message = FailedRequestMessage("S-BSST1", 1)

        every { notificationsSender.send(capture(notificationSlot)) } answers { nothing }

        testInstance.receiveFailedSubmissionMessage(message)

        verify(exactly = 1) { notificationsSender.send(notificationSlot.captured) }
    }

    @Test
    fun `notification failed`() {
        val errorNotificationSlot = slot<SystemNotification>()

        every { webConsumer.getExtSubmission("ext-tab-url") } throws Exception()
        every { notificationsSender.send(capture(errorNotificationSlot)) } answers { nothing }

        testInstance.receiveSubmissionReleaseMessage(message)

        verify { rtNotificationService wasNot called }
        verify(exactly = 1) {
            webConsumer.getExtSubmission("ext-tab-url")
            notificationsSender.send(errorNotificationSlot.captured)
            rabbitTemplate.convertAndSend(BIOSTUDIES_EXCHANGE, NOTIFICATIONS_FAILED_REQUEST_ROUTING_KEY, message)
        }
    }

    private fun mockMessage() {
        every { message.accNo } returns "S-BSST1"
        every { message.extTabUrl } returns "ext-tab-url"
        every { message.extUserUrl } returns "ext-user-url"
    }

    private fun mockSubmitter() {
        every { submitter.fullName } returns "Dr Owner"
        every { submitter.notificationsEnabled } returns true
    }

    private fun mockRabbit() {
        every {
            rabbitTemplate.convertAndSend(BIOSTUDIES_EXCHANGE, NOTIFICATIONS_FAILED_REQUEST_ROUTING_KEY, message)
        } answers { nothing }
    }
}
