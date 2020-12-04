package ebi.ac.uk.util.date

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.time.ZoneOffset.UTC

class InstantTest {
    @Test
    fun `instant as offset at start of day`() {
        val expected = OffsetDateTime.of(2020, 9, 21, 0, 0, 0, 0, UTC)
        val instant = OffsetDateTime.of(2020, 9, 21, 15, 2, 1, 0, UTC).toInstant()

        assertThat(instant.asOffsetAtStartOfDay()).isEqualTo(expected)
    }

    @Test
    fun `instant as offset at end of day`() {
        val expected = OffsetDateTime.of(2020, 9, 21, 23, 59, 59, 0, UTC)
        val instant = OffsetDateTime.of(2020, 9, 21, 15, 2, 1, 0, UTC).toInstant()

        assertThat(instant.asOffsetAtEndOfDay()).isEqualTo(expected)
    }
}
