package ebi.ac.uk.util.collections

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ListExtensionsTest {

    @Test
    fun split() {
        val result = listOf("a", "b", "", "c", "d").split { it.isBlank() }
        assertThat(result).hasSize(2)
        assertThat(result.first()).containsExactly("a", "b")
        assertThat(result.second()).containsExactly("c", "d")
    }

    @Test
    fun splitWhenEmpty() {
        assertThat(listOf("").split { it.isBlank() }).isEmpty()

        assertThat(listOf("", "").split { it.isBlank() }).isEmpty()
    }
}
