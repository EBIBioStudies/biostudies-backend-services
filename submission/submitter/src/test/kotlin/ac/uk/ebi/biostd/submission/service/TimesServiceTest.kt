package ac.uk.ebi.biostd.submission.service

import ac.uk.ebi.biostd.submission.exceptions.InvalidDateFormatException
import ac.uk.ebi.biostd.submission.exceptions.InvalidReleaseDateException
import ac.uk.ebi.biostd.submission.exceptions.PastReleaseDateException
import ac.uk.ebi.biostd.submission.model.SubmitRequest
import ebi.ac.uk.model.extensions.releaseDate
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.time.OffsetDateTime
import java.time.ZoneOffset.UTC

@ExtendWith(MockKExtension::class)
class TimesServiceTest(
    @MockK private val request: SubmitRequest,
    @MockK private val privilegesService: IUserPrivilegesService,
) {
    private val testInstance = TimesService(privilegesService)
    private val testTime = OffsetDateTime.of(2018, 10, 10, 0, 0, 0, 0, UTC)
    private val mockNow = OffsetDateTime.of(2018, 12, 31, 0, 0, 0, 0, UTC)

    @BeforeEach
    fun beforeEach() {
        mockkStatic(OffsetDateTime::class)
        every { OffsetDateTime.now() } returns mockNow
        every { request.previousVersion } returns null
        every { request.submission.releaseDate } returns null
    }

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Nested
    inner class ModificationTime {
        @Test
        fun `calculate modification time`() {
            val times = testInstance.getTimes(request)
            assertThat(times.modificationTime).isEqualTo(mockNow)
        }
    }

    @Nested
    inner class CreationTime {
        @Test
        fun `when exists`() {
            every { request.previousVersion?.creationTime } returns testTime

            val times = testInstance.getTimes(request)
            assertThat(times.createTime).isEqualTo(testTime)
        }

        @Test
        fun `when is new`() {
            val times = testInstance.getTimes(request)
            assertThat(times.createTime).isEqualTo(mockNow)
        }
    }

    @Nested
    inner class ReleaseTime {
        private val previousReleaseTime = OffsetDateTime.of(2016, 10, 10, 0, 0, 0, 0, UTC)

        @BeforeEach
        fun beforeEach() {
            every { request.accNo } returns "S-BSST1"
            every { request.submitter.email } returns "user@test.org"
            every { request.previousVersion?.released } returns true
            every { privilegesService.canSuppress("user@test.org") } returns false
            every { request.previousVersion?.releaseTime } returns previousReleaseTime
            every { request.previousVersion?.creationTime } returns previousReleaseTime
        }

        @Test
        fun `when release date has invalid format`() {
            every { request.submission.releaseDate } returns "2018/10/10"

            val exception = assertThrows<InvalidDateFormatException> { testInstance.getTimes(request) }
            assertThat(exception.message).isEqualTo(
                "Provided date 2018/10/10 could not be parsed. Expected format is YYYY-MM-DD"
            )
        }

        @Test
        fun `when no release date`() {
            val times = testInstance.getTimes(request)
            assertThat(times.released).isFalse()
            assertThat(times.releaseTime).isNull()
        }

        @Test
        fun `when release date is in the past`() {
            every { request.previousVersion } returns null
            every { request.submission.releaseDate } returns "2016-10-10T09:27:04.000Z"

            val error = assertThrows<PastReleaseDateException> { testInstance.getTimes(request) }
            assertThat(error.message).isEqualTo("Release date cannot be in the past")
        }

        @Test
        fun `when updating existing submission`() {
            every { request.submission.releaseDate } returns "2016-10-10T21:16:36.000Z"

            val times = testInstance.getTimes(request)
            assertThat(times.released).isTrue()
            assertThat(times.releaseTime).isEqualTo(previousReleaseTime)
        }

        @Test
        fun `when publishing private submission`() {
            every { request.previousVersion?.released } returns false
            every { request.submission.releaseDate } returns "2018-12-31T09:27:04.000Z"

            val times = testInstance.getTimes(request)
            assertThat(times.released).isTrue()
            assertThat(times.releaseTime).isEqualTo(mockNow)
        }

        @Test
        fun `when updating private submission release date`() {
            val releaseTime = OffsetDateTime.of(2019, 10, 10, 0, 0, 0, 0, UTC)
            val previousReleaseTime = OffsetDateTime.of(2020, 10, 10, 0, 0, 0, 0, UTC)

            every { request.previousVersion?.released } returns false
            every { request.previousVersion?.releaseTime } returns previousReleaseTime
            every { request.submission.releaseDate } returns "2019-10-10T09:27:04.000Z"

            val times = testInstance.getTimes(request)
            assertThat(times.released).isFalse()
            assertThat(times.releaseTime).isEqualTo(releaseTime)
        }

        @Test
        fun `when updating public submission release date`() {
            every { request.submission.releaseDate } returns "2015-10-10T21:16:36.000Z"

            val error = assertThrows<InvalidReleaseDateException> { testInstance.getTimes(request) }
            assertThat(error.message).isEqualTo("The release date of a public study cannot be changed")
        }

        @Test
        fun `when submitter cant suppress`() {
            every { request.previousVersion?.released } returns true
            every { request.submission.releaseDate } returns "2020-10-10T09:27:04.000Z"

            val error = assertThrows<InvalidReleaseDateException> { testInstance.getTimes(request) }
            assertThat(error.message).isEqualTo("The release date of a public study cannot be changed")
        }

        @Test
        fun `when submitter can suppress`() {
            val releaseTime = OffsetDateTime.of(2020, 10, 10, 0, 0, 0, 0, UTC)

            every { request.previousVersion?.released } returns true
            every { privilegesService.canSuppress("user@test.org") } returns true
            every { request.submission.releaseDate } returns "2020-10-10T09:27:04.000Z"

            val times = testInstance.getTimes(request)
            assertThat(times.released).isFalse()
            assertThat(times.releaseTime).isEqualTo(releaseTime)
        }
    }
}
