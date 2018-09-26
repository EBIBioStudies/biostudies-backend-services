package ebi.ac.uk.base

import ebi.ac.uk.funtions.asIsoDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.ZoneOffset

class DateFunctionsTest {

    @Test
    fun toIsoDate() {
        val date = asIsoDate(552355200)
        assertThat(date).isEqualTo(LocalDate.of(1987, 7, 4).atStartOfDay().toInstant(ZoneOffset.UTC))
    }
}

