package uk.ac.ebi.biostd.base

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.ac.ebi.biostd.functions.secondsToInstant
import java.time.LocalDate
import java.time.ZoneOffset

class DateFunctionsTest {
    @Test
    fun toIsoDate() {
        val date = secondsToInstant(552355200)
        assertThat(date).isEqualTo(LocalDate.of(1987, 7, 4).atStartOfDay().toInstant(ZoneOffset.UTC))
    }
}
