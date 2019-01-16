package ebi.ac.uk.util.collections

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicInteger

class CollectionsExtensionsTest {

    @Test
    fun `if list is empty, do nothing`() =
        emptyList<String>().ifNotEmpty { throw IllegalStateException("this should not be executed") }

    @Test
    fun `if list is not empty, do something`() {
        val count = AtomicInteger(0)

        listOf("string1").ifNotEmpty { count.getAndIncrement() }
        assertThat(count).hasValue(1)
    }

    @Test
    fun `list from values`() {
        assertThat(merge(listOf("a"), "b", "c")).isEqualTo(listOf("a", "b", "c"))
    }

    @Test
    fun `list from empty list`() {
        assertThat(merge(emptyList<String>())).isEqualTo(emptyList<String>())
    }

    @Test
    fun `remove first element`() {
        val list: MutableList<String> = mutableListOf("a", "b", "c")
        val first: String = list.removeFirst()

        assertThat(first).isEqualTo("a")
        assertThat(list.contains("a")).isFalse()
        assertThat(list[0]).isEqualTo("b")
        assertThat(list[1]).isEqualTo("c")
    }
}
