package ebi.ac.uk.base

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicInteger

class StringExtTest {

    @Test
    fun isNotEmptyOrNull() {
        assertThat("".isNotBlank()).isFalse
        assertThat(null.isNotBlank()).isFalse
        assertThat("something".isNotBlank()).isTrue
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

    @Test
    fun scape() {
        val original = """hey "friend" """

        assertThat(original.scape("\"")).isEqualTo("""hey \"friend\" """)
    }

    @Nested
    inner class RemoveFirstOccurrence {
        @Test
        fun `remove First Occurrence when exist`() {
            assertThat("abc".removeFirstOccurrence("ab".toRegex())).isEqualTo("c")
        }

        @Test
        fun `remove First Occurrence when there is no match`() {
            assertThat("abc".removeFirstOccurrence("xz".toRegex())).isEqualTo("abc")
        }

        @Test
        fun `remove First Occurrence when multiple`() {
            assertThat("abcab".removeFirstOccurrence("ab".toRegex())).isEqualTo("cab")
        }
    }
}
