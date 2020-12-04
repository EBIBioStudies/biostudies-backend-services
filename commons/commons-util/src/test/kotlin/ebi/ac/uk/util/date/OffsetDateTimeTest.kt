package ebi.ac.uk.util.date

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.time.ZoneOffset.UTC

internal class OffsetDateTimeTest {
    @Test
    fun asIsoTime() {
        val offsetDateTime = OffsetDateTime.of(2018, 1, 3, 4, 55, 22, 0, UTC)
        assertThat(offsetDateTime.asIsoTime()).isEqualTo("2018-01-03T04:55:22Z")
    }

    @Test
    fun fromIsoTime() {
        val date = fromIsoTime("2020-01-28T10:56:41.120Z")

        assertThat(date.year).isEqualTo(2020)
        assertThat(date.monthValue).isEqualTo(1)
        assertThat(date.dayOfMonth).isEqualTo(28)
        assertThat(date.hour).isEqualTo(10)
        assertThat(date.minute).isEqualTo(56)
        assertThat(date.second).isEqualTo(41)
    }

    @Test
    fun toStringDate() {
        val time = OffsetDateTime.of(2018, 9, 21, 2, 3, 45, 0, UTC)
        assertThat(time.toStringDate()).isEqualTo("2018-09-21")
    }

    @Test
    fun toStringInstant() {
        val time = OffsetDateTime.of(2019, 9, 21, 15, 3, 45, 0, UTC)
        assertThat(time.toStringInstant()).isEqualTo("2019-09-21T15:03:45Z")
    }

    @Nested
    inner class IsBeforeOrEqual {
        private val oneDate = OffsetDateTime.now()
        private val anotherDate = oneDate.plusDays(1)

        @Test
        fun `when is before`() {
            assertThat(oneDate.isBeforeOrEqual(anotherDate)).isTrue()
        }

        @Test
        fun `When is equal`() {
            assertThat(oneDate.isBeforeOrEqual(oneDate)).isTrue()
        }

        @Test
        fun `When is greater`() {
            assertThat(anotherDate.isBeforeOrEqual(oneDate)).isFalse()
        }
    }

    @Nested
    inner class Max {
        private val oneDate = OffsetDateTime.now()
        private val anotherDate = oneDate.plusDays(1)

        @Test
        fun `when first one is greater`() {
            assertThat(max(oneDate, anotherDate)).isEqualTo(anotherDate)
        }

        @Test
        fun `when second one is greater`() {
            assertThat(max(anotherDate, oneDate)).isEqualTo(anotherDate)
        }
    }
}
