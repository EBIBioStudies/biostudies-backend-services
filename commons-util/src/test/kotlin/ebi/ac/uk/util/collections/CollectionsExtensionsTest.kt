package ebi.ac.uk.util.collections

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicInteger

class CollectionsExtensionsTest {

    @Test
    fun ifNotEmptyWhenEmpty() {
        emptyList<String>().ifNotEmpty { throw IllegalStateException("should not be executed") }
    }

    @Test
    fun ifNotEmptyWhenNotEmpty() {
        val count = AtomicInteger(0)

        listOf("string1").ifNotEmpty { count.getAndIncrement() }
        assertThat(count).hasValue(1)
    }

    @Test
    fun listFrom() {
        assertThat(merge(listOf("a"), "b", "c")).isEqualTo(listOf("a", "b", "c"))
    }

    @Test
    fun listFromWithEmptyList() {
        assertThat(merge(emptyList<String>())).isEqualTo(emptyList<String>())
    }

    @Test
    fun removeFirst() {
        val list: MutableList<String> = mutableListOf("a", "b", "c")
        val first: String = list.removeFirst()

        assertThat(first).isEqualTo("a")
        assertThat(list.contains("a")).isFalse()
        assertThat(list[0]).isEqualTo("b")
        assertThat(list[1]).isEqualTo("c")
    }
}
