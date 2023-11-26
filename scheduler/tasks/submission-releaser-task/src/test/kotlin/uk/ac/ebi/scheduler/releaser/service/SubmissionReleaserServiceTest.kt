package uk.ac.ebi.scheduler.releaser.service

import ac.uk.ebi.biostd.client.dto.ReleaseRequestDto
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.persistence.doc.db.reactive.repositories.SubmissionReleaserRepository
import ac.uk.ebi.biostd.persistence.doc.db.repositories.ReleaseData
import ebi.ac.uk.util.date.asOffsetAtEndOfDay
import ebi.ac.uk.util.date.toDate
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.scheduler.releaser.config.NotificationTimes
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset.UTC
import java.util.Date

@ExtendWith(MockKExtension::class)
class SubmissionReleaserServiceTest(
    @MockK private val bioWebClient: BioWebClient,
    @MockK private val notificationTimes: NotificationTimes,
    @MockK private val releaserRepository: SubmissionReleaserRepository,
    @MockK private val eventsPublisherService: EventsPublisherService,
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
    fun `notify submission release`() = runTest {
        val firstWarningData = ReleaseData("S-BSST0", "owner0@mail.org", "S-BSST/000/S-BSST0")
        val secondWarningData = ReleaseData("S-BSST1", "owner1@mail.org", "S-BSST/001/S-BSST1")
        val thirdWarningData = ReleaseData("S-BSST2", "owner2@mail.org", "S-BSST/002/S-BSST2")

        mockNotificationQuery(month = 11, day = 20, response = firstWarningData)
        mockNotificationQuery(month = 10, day = 21, response = secondWarningData)
        mockNotificationQuery(month = 9, day = 28, response = thirdWarningData)

        testInstance.notifySubmissionReleases()

        verify(exactly = 1) { eventsPublisherService.subToBePublished("S-BSST0", "owner0@mail.org") }
        verify(exactly = 1) { eventsPublisherService.subToBePublished("S-BSST1", "owner1@mail.org") }
        verify(exactly = 1) { eventsPublisherService.subToBePublished("S-BSST2", "owner2@mail.org") }
    }

    @Test
    fun `release daily submissions`(
        @MockK to: Date
    ) = runTest {
        val requestSlot = slot<ReleaseRequestDto>()
        val released = ReleaseData("S-BSST0", "owner0@mail.org", "S-BSST/000/S-BSST0")

        every { mockNow.asOffsetAtEndOfDay().toDate() } returns to
        every { bioWebClient.releaseSubmission(capture(requestSlot)) } answers { nothing }
        every { releaserRepository.findAllUntil(to) } returns flowOf(released)

        testInstance.releaseDailySubmissions()

        val releaseRequest = requestSlot.captured
        verify(exactly = 1) { bioWebClient.releaseSubmission(releaseRequest) }
        assertThat(releaseRequest.accNo).isEqualTo("S-BSST0")
        assertThat(releaseRequest.owner).isEqualTo("owner0@mail.org")
        assertThat(releaseRequest.relPath).isEqualTo("S-BSST/000/S-BSST0")
    }

    @Test
    fun `generate ftp links`() = runTest {
        val released = ReleaseData("S-BSST0", "owner0@mail.org", "S-BSST/000/S-BSST0")

        every { releaserRepository.findAllReleased() } returns flowOf(released)
        coEvery { bioWebClient.generateFtpLinks("S-BSST0") } answers { nothing }

        testInstance.generateFtpLinks()
        coVerify(exactly = 1) { bioWebClient.generateFtpLinks("S-BSST0") }
    }

    private fun mockNotificationQuery(month: Int, day: Int, response: ReleaseData) {
        val from = OffsetDateTime.of(2020, month, day, 0, 0, 0, 0, UTC).toDate()
        val to = OffsetDateTime.of(2020, month, day, 23, 59, 59, 0, UTC).toDate()

        every { releaserRepository.findAllBetween(from, to) } returns flowOf(response)
        every { eventsPublisherService.subToBePublished(response.accNo, response.owner) } answers { nothing }
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
        mockkStatic("ebi.ac.uk.util.date.OffsetDateTimeKt")
        mockkStatic("ac.uk.ebi.biostd.client.extensions.BioWebClientExtKt")
        mockkStatic("ebi.ac.uk.extended.model.ExtSubmissionExtensionsKt")
    }
}
