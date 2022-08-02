package uk.ac.ebi.scheduler.releaser.service

import ac.uk.ebi.biostd.client.dto.ReleaseRequestDto
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ebi.ac.uk.util.date.asOffsetAtEndOfDay
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.events.service.EventsPublisherService
import uk.ac.ebi.scheduler.releaser.config.NotificationTimes
import uk.ac.ebi.scheduler.releaser.model.ReleaseData
import uk.ac.ebi.scheduler.releaser.persistence.ReleaserRepository
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset.UTC

@ExtendWith(MockKExtension::class)
class SubmissionReleaserServiceTest(
    @MockK private val bioWebClient: BioWebClient,
    @MockK private val notificationTimes: NotificationTimes,
    @MockK private val releaserRepository: ReleaserRepository,
    @MockK private val eventsPublisherService: EventsPublisherService
) {
    private val mockNow = OffsetDateTime.of(2020, 9, 21, 10, 11, 0, 0, UTC).toInstant()
    private val testInstance =
        SubmissionReleaserService(bioWebClient, notificationTimes, releaserRepository, eventsPublisherService)

    @AfterEach
    fun afterEach() = clearAllMocks()

    @BeforeEach
    fun beforeEach() {
        mockInstantNow()
        mockNotificationTimes()
        mockExtensionFunctions()
    }

    @Test
    fun `notify submission release`() {
        val firstWarningData = ReleaseData("S-BSST0", "owner0@mail.org", "S-BSST/000/S-BSST0")
        val secondWarningData = ReleaseData("S-BSST1", "owner1@mail.org", "S-BSST/001/S-BSST1")
        val thirdWarningData = ReleaseData("S-BSST2", "owner2@mail.org", "S-BSST/002/S-BSST2")

        mockNotificationQuery(month = 11, day = 20, response = firstWarningData)
        mockNotificationQuery(month = 10, day = 21, response = secondWarningData)
        mockNotificationQuery(month = 9, day = 28, response = thirdWarningData)

        testInstance.notifySubmissionReleases()

        verify(exactly = 1) { eventsPublisherService.submissionReleased("S-BSST0", "owner0@mail.org") }
        verify(exactly = 1) { eventsPublisherService.submissionReleased("S-BSST1", "owner1@mail.org") }
        verify(exactly = 1) { eventsPublisherService.submissionReleased("S-BSST2", "owner2@mail.org") }
    }

    @Test
    fun `release daily submissions`() {
        val requestSlot = slot<ReleaseRequestDto>()
        val released = ReleaseData("S-BSST0", "owner0@mail.org", "S-BSST/000/S-BSST0")

        every { bioWebClient.releaseSubmission(capture(requestSlot)) } answers { nothing }
        every { releaserRepository.findAllUntil(mockNow.asOffsetAtEndOfDay().toLocalDate()) } returns listOf(released)

        testInstance.releaseDailySubmissions()

        val releaseRequest = requestSlot.captured
        verify(exactly = 1) { bioWebClient.releaseSubmission(releaseRequest) }
        assertThat(releaseRequest.accNo).isEqualTo("S-BSST0")
        assertThat(releaseRequest.owner).isEqualTo("owner0@mail.org")
        assertThat(releaseRequest.relPath).isEqualTo("S-BSST/000/S-BSST0")
    }

    @Test
    fun `generate ftp links`() {
        val released = ReleaseData("S-BSST0", "owner0@mail.org", "S-BSST/000/S-BSST0")

        every { releaserRepository.findAllReleased() } returns listOf(released)
        every { bioWebClient.generateFtpLink("S-BSST/000/S-BSST0") } answers { nothing }

        testInstance.generateFtpLinks()
        verify(exactly = 1) { bioWebClient.generateFtpLink("S-BSST/000/S-BSST0") }
    }

    private fun mockNotificationQuery(month: Int, day: Int, response: ReleaseData) {
        val from = OffsetDateTime.of(2020, month, day, 0, 0, 0, 0, UTC).toLocalDate()
        val to = OffsetDateTime.of(2020, month, day, 23, 59, 59, 0, UTC).toLocalDate()

        every { releaserRepository.findAllBetween(from, to) } returns listOf(response)
        every { eventsPublisherService.submissionReleased(response.accNo, response.owner) } answers { nothing }
    }

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
