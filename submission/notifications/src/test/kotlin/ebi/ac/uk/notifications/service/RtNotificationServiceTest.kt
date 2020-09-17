package ebi.ac.uk.notifications.service

import ebi.ac.uk.extended.model.ExtAccessTag
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtProcessingStatus.PROCESSED
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.ExtSubmissionMethod.PAGE_TAB
import ebi.ac.uk.extended.model.ExtTag
import ebi.ac.uk.notifications.api.RtClient
import ebi.ac.uk.notifications.persistence.model.SubmissionRT
import ebi.ac.uk.notifications.persistence.service.NotificationPersistenceService
import ebi.ac.uk.notifications.util.TemplateLoader
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.OffsetDateTime
import java.time.ZoneOffset

@ExtendWith(MockKExtension::class)
class RtNotificationServiceTest(
    @MockK private val rtClient: RtClient,
    @MockK private val templateLoader: TemplateLoader,
    @MockK private val notificationPersistenceService: NotificationPersistenceService
) {
    private val testInstance = RtNotificationService(rtClient, templateLoader, notificationPersistenceService)

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun `successful submission`() {
        val submission = testSubmission(version = 1)
        val ticket = SubmissionRT("S-TEST1", "78910")
        every { templateLoader.loadTemplate("successful-submission.txt") } returns "submit"
        every { notificationPersistenceService.findTicketId("S-TEST1") } returns null
        every { notificationPersistenceService.saveRtNotification("S-TEST1", "78910") } returns ticket
        every {
            rtClient.createTicket("S-TEST1", "BioStudies Submission - S-TEST1", "owner@mail.org", "submit")
        } returns "78910"

        testInstance.notifySuccessfulSubmission(submission, "Dr. Owner", "ui-url")

        verify(exactly = 1) { notificationPersistenceService.findTicketId("S-TEST1") }
        verify(exactly = 1) { notificationPersistenceService.saveRtNotification("S-TEST1", "78910") }
        verify(exactly = 1) {
            rtClient.createTicket("S-TEST1", "BioStudies Submission - S-TEST1", "owner@mail.org", "submit")
        }
    }

    @Test
    fun `successful resubmission`() {
        val submission = testSubmission(version = 2)

        every { rtClient.commentTicket("78910", "resubmit") } answers { nothing }
        every { notificationPersistenceService.findTicketId("S-TEST1") } returns "78910"
        every { templateLoader.loadTemplate("successful-resubmission.txt") } returns "resubmit"

        testInstance.notifySuccessfulSubmission(submission, "Dr. Owner", "ui-url")

        verify(exactly = 1) { rtClient.commentTicket("78910", "resubmit") }
        verify(exactly = 1) { notificationPersistenceService.findTicketId("S-TEST1") }
    }

    @Test
    fun `submission release non existing ticket`() {
        val submission = testSubmission(version = 1)
        val ticket = SubmissionRT("S-TEST1", "78910")
        every { templateLoader.loadTemplate(SUBMISSION_RELEASE_TEMPLATE) } returns "release"
        every { notificationPersistenceService.findTicketId("S-TEST1") } returns null
        every { notificationPersistenceService.saveRtNotification("S-TEST1", "78910") } returns ticket
        every {
            rtClient.createTicket("S-TEST1", "BioStudies Submission - S-TEST1", "owner@mail.org", "release")
        } returns "78910"

        testInstance.notifySubmissionRelease(submission, "Dr. Owner", "ui-url")

        verify(exactly = 1) { notificationPersistenceService.findTicketId("S-TEST1") }
        verify(exactly = 1) { notificationPersistenceService.saveRtNotification("S-TEST1", "78910") }
        verify(exactly = 1) {
            rtClient.createTicket("S-TEST1", "BioStudies Submission - S-TEST1", "owner@mail.org", "release")
        }
    }

    @Test
    fun `submission release existing ticket`() {
        val submission = testSubmission(version = 1)

        every { rtClient.commentTicket("78910", "release") } answers { nothing }
        every { notificationPersistenceService.findTicketId("S-TEST1") } returns "78910"
        every { templateLoader.loadTemplate(SUBMISSION_RELEASE_TEMPLATE) } returns "release"

        testInstance.notifySubmissionRelease(submission, "Dr. Owner", "ui-url")

        verify(exactly = 1) { rtClient.commentTicket("78910", "release") }
        verify(exactly = 1) { notificationPersistenceService.findTicketId("S-TEST1") }
    }

    private fun testSubmission(version: Int): ExtSubmission {
        val time = OffsetDateTime.of(2019, 9, 21, 10, 30, 34, 15, ZoneOffset.UTC)
        return ExtSubmission(
            accNo = "S-TEST1",
            version = version,
            owner = "owner@mail.org",
            submitter = "submitter@mail.org",
            title = "Test Submission",
            method = PAGE_TAB,
            relPath = "/a/rel/path",
            rootPath = "/a/root/path",
            released = false,
            secretKey = "a-secret-key",
            status = PROCESSED,
            releaseTime = time,
            modificationTime = time,
            creationTime = time,
            attributes = listOf(ExtAttribute("AttachTo", "BioImages")),
            tags = listOf(ExtTag("component", "web")),
            accessTags = listOf(ExtAccessTag("BioImages")),
            section = ExtSection(type = "Study")
        )
    }
}
