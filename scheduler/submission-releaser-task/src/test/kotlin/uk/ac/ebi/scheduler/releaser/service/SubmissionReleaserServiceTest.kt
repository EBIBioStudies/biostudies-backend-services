package uk.ac.ebi.scheduler.releaser.service

import ac.uk.ebi.biostd.client.dto.ExtPageQuery
import ac.uk.ebi.biostd.client.extensions.getExtSubmissionsAsSequence
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.isProject
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.events.service.EventsPublisherService
import uk.ac.ebi.scheduler.releaser.config.NotificationTimes
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset.UTC

@ExtendWith(MockKExtension::class)
class SubmissionReleaserServiceTest(
    @MockK private val bioWebClient: BioWebClient,
    @MockK private val notificationTimes: NotificationTimes,
    @MockK private val eventsPublisherService: EventsPublisherService
) {
    private val mockNow = OffsetDateTime.of(2020, 9, 21, 10, 11, 0, 0, UTC).toInstant()
    private val testInstance = SubmissionReleaserService(bioWebClient, notificationTimes, eventsPublisherService)

    @AfterEach
    fun afterEach() = clearAllMocks()

    @BeforeEach
    fun beforeEach() {
        mockInstantNow()
        mockNotificationTimes()
        mockExtensionFunctions()
    }

    @Test
    fun `notify submission release`(
        @MockK firstWarningSubmission: ExtSubmission,
        @MockK secondWarningSubmission: ExtSubmission,
        @MockK thirdWarningSubmission: ExtSubmission
    ) {
        val firstWarningQuery = createNotifyExtPageQuery(month = 11, day = 20)
        val secondWarningQuery = createNotifyExtPageQuery(month = 10, day = 21)
        val thirdWarningQuery = createNotifyExtPageQuery(month = 9, day = 28)

        mockExtSubmissionsQuery(firstWarningQuery, firstWarningSubmission)
        mockExtSubmissionsQuery(secondWarningQuery, secondWarningSubmission)
        mockExtSubmissionsQuery(thirdWarningQuery, thirdWarningSubmission)
        mockReleaseNotificationEvent(firstWarningSubmission)
        mockReleaseNotificationEvent(secondWarningSubmission)
        mockReleaseNotificationEvent(thirdWarningSubmission)

        testInstance.notifySubmissionReleases()
        verify(exactly = 1) { bioWebClient.getExtSubmissionsAsSequence(firstWarningQuery) }
        verify(exactly = 1) { bioWebClient.getExtSubmissionsAsSequence(secondWarningQuery) }
        verify(exactly = 1) { bioWebClient.getExtSubmissionsAsSequence(thirdWarningQuery) }
        verify(exactly = 1) { eventsPublisherService.submissionReleased(firstWarningSubmission) }
        verify(exactly = 1) { eventsPublisherService.submissionReleased(secondWarningSubmission) }
        verify(exactly = 1) { eventsPublisherService.submissionReleased(thirdWarningSubmission) }
    }

    @Test
    fun `release daily submissions`(@MockK releaseSubmission: ExtSubmission) {
        val releaseQuery = createReleaseExtPageQuery()

        mockExtSubmissionsQuery(releaseQuery, releaseSubmission)
        every { releaseSubmission.isProject } returns false
        every { releaseSubmission.released } returns false
        every { releaseSubmission.copy(released = true) } returns releaseSubmission
        every { bioWebClient.submitExt(releaseSubmission) } returns releaseSubmission

        testInstance.releaseDailySubmissions()
        verify(exactly = 1) { bioWebClient.submitExt(releaseSubmission) }
        verify(exactly = 1) { bioWebClient.getExtSubmissionsAsSequence(releaseQuery) }
    }

    @Test
    fun `release with project`(@MockK project: ExtSubmission) {
        val releaseQuery = createReleaseExtPageQuery()

        every { project.isProject } returns true
        mockExtSubmissionsQuery(releaseQuery, project)

        testInstance.releaseDailySubmissions()
        verify(exactly = 0) { bioWebClient.submitExt(project) }
        verify(exactly = 1) { bioWebClient.getExtSubmissionsAsSequence(releaseQuery) }
    }

    @Test
    fun `generate ftp links`(@MockK publicSubmission: ExtSubmission) {
        val relPath = "S-BSST/012/S-BSST112"
        val query = ExtPageQuery(released = true)

        mockExtSubmissionsQuery(query, publicSubmission)
        every { publicSubmission.isProject } returns false
        every { publicSubmission.relPath } returns relPath
        every { publicSubmission.accNo } returns "S-BSST112"
        every { bioWebClient.generateFtpLink(relPath) } answers { nothing }

        testInstance.generateFtpLinks()
        verify(exactly = 1) {
            bioWebClient.generateFtpLink(relPath)
            bioWebClient.getExtSubmissionsAsSequence(query)
        }
    }

    @Test
    fun `generate ftp links for projects`(@MockK publicSubmission: ExtSubmission) {
        val relPath = "S-BSST/012/S-BSST112"
        val query = ExtPageQuery(released = true)

        mockExtSubmissionsQuery(query, publicSubmission)
        every { publicSubmission.isProject } returns true
        every { publicSubmission.relPath } returns relPath

        testInstance.generateFtpLinks()
        verify(exactly = 0) { bioWebClient.generateFtpLink(relPath) }
        verify(exactly = 1) { bioWebClient.getExtSubmissionsAsSequence(query) }
    }

    private fun createNotifyExtPageQuery(month: Int, day: Int) = ExtPageQuery(
        released = false,
        fromRTime = OffsetDateTime.of(2020, month, day, 0, 0, 0, 0, UTC),
        toRTime = OffsetDateTime.of(2020, month, day, 23, 59, 59, 0, UTC))

    private fun createReleaseExtPageQuery() =
        ExtPageQuery(toRTime = OffsetDateTime.of(2020, 9, 21, 23, 59, 59, 0, UTC), released = false)

    private fun mockExtSubmissionsQuery(query: ExtPageQuery, response: ExtSubmission) =
        every { bioWebClient.getExtSubmissionsAsSequence(eq(query)) } returns sequenceOf(response)

    private fun mockReleaseNotificationEvent(extSubmission: ExtSubmission) =
        every { eventsPublisherService.submissionReleased(extSubmission) } answers { nothing }

    private fun mockInstantNow() {
        mockkStatic(Instant::class)
        every { Instant.now() } returns mockNow
    }

    private fun mockNotificationTimes() {
        every { notificationTimes.firstWarningDays } returns 60
        every { notificationTimes.secondWarningDays } returns 30
        every { notificationTimes.thirdWarningDays } returns 7
    }

    private fun mockExtensionFunctions() {
        mockkStatic("ac.uk.ebi.biostd.client.extensions.BioWebClientExtKt")
        mockkStatic("ebi.ac.uk.extended.model.ExtSubmissionExtensionsKt")
    }
}
