package ebi.ac.uk.base

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class IntExtTest {

    @Test
    fun pow() {
        assertThat(2.pow(5)).isEqualTo(32)
    }
}
