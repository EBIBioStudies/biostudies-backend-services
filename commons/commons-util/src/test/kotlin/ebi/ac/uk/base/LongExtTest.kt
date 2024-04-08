package ebi.ac.uk.base

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class LongExtTest {
    @Test
    fun lastDigits() {
        assertThat(10L.lastDigits(2)).isEqualTo(10)
        assertThat(1981L.lastDigits(3)).isEqualTo(981)
    }
}
