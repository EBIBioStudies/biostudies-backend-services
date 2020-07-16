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
import ebi.ac.uk.test.createFile
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import java.time.OffsetDateTime
import java.time.ZoneOffset

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
class RtNotificationServiceTest(
    @MockK private val rtClient: RtClient,
    @MockK private val resource: Resource,
    @MockK private val resourceLoader: ResourceLoader,
    @MockK private val notificationPersistenceService: NotificationPersistenceService,
    private val temporaryFolder: TemporaryFolder
) {
    private val testInstance = RtNotificationService(rtClient, resourceLoader, notificationPersistenceService)

    @Test
    fun `successful submission`() {
        val submission = testSubmission(version = 1)
        val ticket = SubmissionRT("S-TEST1", "78910")
        val file = temporaryFolder.createFile("successful-submission.txt", "submit")

        every { resource.inputStream } returns file.inputStream()
        every { notificationPersistenceService.findTicketId("S-TEST1") } returns null
        every { notificationPersistenceService.saveRtNotification("S-TEST1", "78910") } returns ticket
        every { resourceLoader.getResource("classpath:templates/$SUCCESSFUL_SUBMISSION_TEMPLATE") } returns resource
        every {
            rtClient.createTicket("S-TEST1", "BioStudies Successful Submission - S-TEST1", "owner@mail.org", "submit")
        } returns "78910"

        testInstance.notifySuccessfulSubmission(submission, "Dr. Owner", "ui-url")

        verify(exactly = 1) { notificationPersistenceService.findTicketId("S-TEST1") }
        verify(exactly = 1) { notificationPersistenceService.saveRtNotification("S-TEST1", "78910") }
        verify(exactly = 1) { resourceLoader.getResource("classpath:templates/$SUCCESSFUL_SUBMISSION_TEMPLATE") }
        verify(exactly = 1) {
            rtClient.createTicket("S-TEST1", "BioStudies Successful Submission - S-TEST1", "owner@mail.org", "submit")
        }
    }

    @Test
    fun `successful resubmission`() {
        val submission = testSubmission(version = 2)
        val file = temporaryFolder.createFile("successful-resubmission.txt", "resubmit")

        every { resource.inputStream } returns file.inputStream()
        every { rtClient.commentTicket("78910", "resubmit") } answers { nothing }
        every { notificationPersistenceService.findTicketId("S-TEST1") } returns "78910"
        every { resourceLoader.getResource("classpath:templates/$SUCCESSFUL_RESUBMISSION_TEMPLATE") } returns resource

        testInstance.notifySuccessfulSubmission(submission, "Dr. Owner", "ui-url")

        verify(exactly = 1) { rtClient.commentTicket("78910", "resubmit") }
        verify(exactly = 1) { notificationPersistenceService.findTicketId("S-TEST1") }
        verify(exactly = 1) { resourceLoader.getResource("classpath:templates/$SUCCESSFUL_RESUBMISSION_TEMPLATE") }
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
