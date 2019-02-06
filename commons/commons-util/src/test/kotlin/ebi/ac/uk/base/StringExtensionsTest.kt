package ebi.ac.uk.base

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicInteger

class StringExtensionsTest {

    @Test
    fun isNotEmptyOrNull() {
        assertThat("".isNotBlank()).isFalse()
        assertThat(null.isNotBlank()).isFalse()
        assertThat("something".isNotBlank()).isTrue()
    }

    @Test
    fun applyIfNotBlankWhenBlank() {
        "".applyIfNotBlank { throw IllegalStateException("should not be executed") }
        null.applyIfNotBlank { throw IllegalStateException("should not be executed") }
    }

    @Test
    fun applyIfNotBlankWhenIsNotBlank() {
        val count = AtomicInteger(0)
        "something".applyIfNotBlank { count.incrementAndGet() }

        assertThat(count).hasValue(1)
    }
}
