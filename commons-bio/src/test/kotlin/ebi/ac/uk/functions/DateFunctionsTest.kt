package ebi.ac.uk.functions

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant

internal class DateFunctionsTest {

    @Test
    fun asIsoDate() {
        assertThat(asInstant(552398400)).isEqualTo(Instant.parse("1987-07-04T12:00:00Z"))
    }
}
