package ebi.ac.uk.functions

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.ac.ebi.biostd.functions.secondsToInstant
import java.time.Instant

internal class DateFunctionsTest {
    @Test
    fun asIsoDate() {
        assertThat(secondsToInstant(552398400)).isEqualTo(Instant.parse("1987-07-04T12:00:00Z"))
    }
}
