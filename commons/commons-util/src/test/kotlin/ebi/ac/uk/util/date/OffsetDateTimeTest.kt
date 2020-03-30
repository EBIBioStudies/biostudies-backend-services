package ebi.ac.uk.util.date

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset

internal class OffsetDateTimeTest {

    @Test
    fun asIsoTime() {
        val date = createInstant(2018, 1, 3, 4, 55, 22)
        val offsetDateTime = OffsetDateTime.ofInstant(date, ZoneId.of("UTC"))
        assertThat(offsetDateTime.asIsoTime()).isEqualTo("2018-02-03T04:55:22Z")
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
        val time = OffsetDateTime.of(2018, 9, 21, 2, 3, 45, 0, ZoneOffset.UTC)
        assertThat(time.toStringDate()).isEqualTo("2018-09-21")
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
