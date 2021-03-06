package ac.uk.ebi.biostd.submission.service

import ac.uk.ebi.biostd.submission.exceptions.InvalidDateFormatException
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.time.OffsetDateTime
import java.time.ZoneOffset

private const val ACC_NO = "ABC456"

@ExtendWith(MockKExtension::class)
class TimesServiceTest {
    private val testInstance = TimesService()
    private val testTime = OffsetDateTime.of(2018, 10, 10, 0, 0, 0, 0, ZoneOffset.UTC)
    private val mockNow = OffsetDateTime.of(2018, 12, 31, 0, 0, 0, 0, ZoneOffset.UTC)

    @BeforeEach
    fun beforeEach() {
        mockkStatic(OffsetDateTime::class)
        every { OffsetDateTime.now() } returns mockNow
    }

    @Nested
    inner class ModificationTime {
        @Test
        fun `calculate modification time`() {
            val times = testInstance.getTimes(TimesRequest(ACC_NO))
            assertThat(times.modificationTime).isEqualTo(mockNow)
        }
    }

    @Nested
    inner class CreationTime {
        @Test
        fun `when exists`() {
            val times = testInstance.getTimes(TimesRequest(accNo = ACC_NO, creationTime = testTime))
            assertThat(times.createTime).isEqualTo(testTime)
        }

        @Test
        fun `when is new`() {
            val times = testInstance.getTimes(TimesRequest(ACC_NO))
            assertThat(times.createTime).isEqualTo(mockNow)
        }
    }

    @Nested
    inner class ReleaseTime {
        @Test
        fun `when release date with invalid format`() {
            val exception = assertThrows<InvalidDateFormatException> {
                testInstance.getTimes(TimesRequest(ACC_NO, "2018/10/10"))
            }

            assertThat(exception.message).isEqualTo(
                "Provided date 2018/10/10 could not be parsed. Expected format is YYYY-MM-DD"
            )
        }

        @Test
        fun `when release date with valid format`() {
            val releaseTime = OffsetDateTime.of(2019, 10, 10, 0, 0, 0, 0, ZoneOffset.UTC)
            val times = testInstance.getTimes(TimesRequest(ACC_NO, "2019-10-10T09:27:04.000Z"))

            assertThat(times.releaseTime).isEqualTo(releaseTime)
        }

        @Test
        fun `when no release date`() {
            val times = testInstance.getTimes(TimesRequest(ACC_NO))
            assertThat(times.releaseTime).isNull()
        }
    }
}
