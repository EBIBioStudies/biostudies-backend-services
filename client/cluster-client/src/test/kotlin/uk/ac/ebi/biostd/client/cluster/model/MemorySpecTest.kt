package uk.ac.ebi.biostd.client.cluster.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class MemorySpecTest {
    @Test
    fun oneGbMemorySpec() {
        val one = MemorySpec.ONE_GB
        assertThat(one.toString()).isEqualTo("1024")
    }

    @Test
    fun eightGbMemorySpec() {
        val one = MemorySpec.EIGHT_GB
        assertThat(one.toString()).isEqualTo("8192")
    }

    @Test
    fun sixteenGbMemorySpec() {
        val one = MemorySpec.SIXTEEN_GB
        assertThat(one.toString()).isEqualTo("16384")
    }
}
