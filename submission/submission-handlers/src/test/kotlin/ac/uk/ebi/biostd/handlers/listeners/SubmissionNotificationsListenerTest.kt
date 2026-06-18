package ac.uk.ebi.biostd.handlers.listeners

import ac.uk.ebi.biostd.common.events.BIOSTUDIES_EXCHANGE
import ac.uk.ebi.biostd.common.properties.NotificationProperties
import ac.uk.ebi.biostd.handlers.api.BioStudiesWebConsumer
import ac.uk.ebi.biostd.handlers.config.NOTIFICATIONS_FAILED_REQUEST_ROUTING_KEY
import ac.uk.ebi.biostd.persistence.common.service.NotificationLogDataService
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
class SubmissionNotificationsListenerTest(
    @param:MockK private val submitter: ExtUser,
    @param:MockK private val submission: ExtSubmission,
    @param:MockK private val rabbitTemplate: RabbitTemplate,
    @param:MockK private val webConsumer: BioStudiesWebConsumer,
    @param:MockK private val notificationsSender: NotificationsSender,
    @param:MockK private val rtNotificationService: RtNotificationService,
    @param:MockK private val notificationProperties: NotificationProperties,
    @param:MockK private val notificationLogDataService: NotificationLogDataService,
) {
    private val message: SubmissionMessage =
        SubmissionMessage(
            accNo = "S-BSST1",
            pagetabUrl = "pagetab-url",
            extTabUrl = "ext-tab-url",
            extUserUrl = "ext-user-url",
            eventTime = "2026-05-19T14:01:00Z",
        )
    private val testInstance =
        SubmissionNotificationsListener(
            rabbitTemplate,
            webConsumer,
            notificationsSender,
            rtNotificationService,
            notificationProperties,
            notificationLogDataService,
        )

    @BeforeEach
    fun beforeEach() {
        mockRabbit()
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
        coEvery { notificationLogDataService.logNotification(any(), any(), any()) } returns Unit
    }

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun `receive submission message`() {
        testInstance.receiveSubmissionMessage(message)

        verify { rabbitTemplate wasNot called }
        coVerify(exactly = 1) {
            webConsumer.getExtSubmission("ext-tab-url")
            rtNotificationService.notifySuccessfulSubmission(submission, "Dr Owner", "ui-url", "st-url")
            notificationLogDataService.logNotification("S-BSST1", "Successful Submission Notification", message)
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
        coVerify(exactly = 1) {
            webConsumer.getExtSubmission("ext-tab-url")
            rtNotificationService.notifySuccessfulSubmission(submission, "Dr Owner", "ui-url/bioimages", "st-url")
            notificationLogDataService.logNotification("S-BSST1", "Successful Submission Notification", message)
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
        coVerify(exactly = 1) {
            notificationLogDataService.logNotification("S-BSST1", "Successful Submission Notification", message)
        }
    }

    @Test
    fun `receive submission release message`() {
        testInstance.receiveSubmissionReleaseMessage(message)

        verify { rabbitTemplate wasNot called }
        coVerify(exactly = 1) {
            webConsumer.getExtSubmission("ext-tab-url")
            rtNotificationService.notifySubmissionRelease(submission, "Dr Owner", "ui-url", "st-url")
            notificationLogDataService.logNotification("S-BSST1", "Release Notification", message)
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
        coVerify(exactly = 1) {
            notificationLogDataService.logNotification("S-BSST1", "Release Notification", message)
        }
    }

    @Test
    fun `notify failed submission`() =
        runTest {
            val notificationSlot = slot<SystemNotification>()
            val message = FailedRequestMessage("S-BSST1", 1)

            coEvery { notificationsSender.send(capture(notificationSlot)) } answers { nothing }

            testInstance.receiveFailedSubmissionMessage(message)

            coVerify(exactly = 1) { notificationsSender.send(notificationSlot.captured) }
        }

    @Test
    fun `notification failed`() {
        val errorNotificationSlot = slot<SystemNotification>()

        every { webConsumer.getExtSubmission("ext-tab-url") } throws Exception("error message")
        coEvery { notificationsSender.send(capture(errorNotificationSlot)) } answers { nothing }
        coEvery {
            notificationLogDataService.logNotificationError(
                "S-BSST1",
                any(),
                "Release Notification",
                message,
            )
        } returns Unit

        testInstance.receiveSubmissionReleaseMessage(message)

        verify { rtNotificationService wasNot called }
        coVerify(exactly = 1) {
            webConsumer.getExtSubmission("ext-tab-url")
            notificationsSender.send(errorNotificationSlot.captured)
            rabbitTemplate.convertAndSend(BIOSTUDIES_EXCHANGE, NOTIFICATIONS_FAILED_REQUEST_ROUTING_KEY, message)
            notificationLogDataService.logNotificationError(
                "S-BSST1",
                any(),
                "Release Notification",
                message,
            )
        }
    }

    @Test
    fun `notification failed is logged`() {
        val errorNotificationSlot = slot<SystemNotification>()

        every { webConsumer.getExtSubmission("ext-tab-url") } throws Exception("error message")
        coEvery { notificationsSender.send(capture(errorNotificationSlot)) } answers { nothing }
        coEvery {
            notificationLogDataService.logNotificationError(
                "S-BSST1",
                any(),
                "Release Notification",
                message,
            )
        } returns Unit

        testInstance.receiveSubmissionReleaseMessage(message)

        verify { rtNotificationService wasNot called }
        coVerify(exactly = 1) {
            webConsumer.getExtSubmission("ext-tab-url")
            notificationsSender.send(errorNotificationSlot.captured)
            rabbitTemplate.convertAndSend(BIOSTUDIES_EXCHANGE, NOTIFICATIONS_FAILED_REQUEST_ROUTING_KEY, message)
            notificationLogDataService.logNotificationError(
                "S-BSST1",
                any(),
                "Release Notification",
                message,
            )
        }
    }

    private fun mockSubmitter() {
        every { submitter.email } returns "submitter@ebi.ac.uk"
        every { submitter.fullName } returns "Dr Owner"
        every { submitter.notificationsEnabled } returns true
    }

    private fun mockRabbit() {
        every {
            rabbitTemplate.convertAndSend(BIOSTUDIES_EXCHANGE, NOTIFICATIONS_FAILED_REQUEST_ROUTING_KEY, message)
        } answers { nothing }
    }
}
