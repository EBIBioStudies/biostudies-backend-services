package ac.uk.ebi.pmc.persistence.utils

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PmcUtilsTest {
    @Test
    fun extractSequence() {
        assertThat(PmcUtils.extractSequence("bs-07-02-2024-6.txt.gz")).isEqualTo(2024_02_07_6)
        assertThat(PmcUtils.extractSequence("bs-21-04-2024-47.txt.gz")).isEqualTo(2024_04_21_47)
        assertThat(PmcUtils.extractSequence("bs-14-11-2023-168.txt.gz")).isEqualTo(2023_11_14_168)
    }
}
