package ebi.ac.uk.base

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicInteger

class BooleanExtTest {
    @Test
    fun `orFalse when not nullable`() {
        assertThat(false.orFalse()).isEqualTo(false)
        assertThat(true.orFalse()).isEqualTo(true)
    }

    @Test
    fun `orFalse when nullable`() {
        val nullable: Boolean? = false
        assertThat(nullable).isEqualTo(false)
    }

    @Test
    fun `ifTrue when true`() {
        val count = AtomicInteger(0)
        true.ifTrue { count.incrementAndGet() }

        assertThat(count).hasValue(1)
    }

    @Test
    fun `ifTrue with false`() {
        val count = AtomicInteger(0)
        false.ifTrue { count.incrementAndGet() }

        assertThat(count).hasValue(0)
    }

    @Test
    fun `fold when true`() {
        val count = AtomicInteger(0)
        true.fold({ count.addAndGet(1) }, { count.addAndGet(2) })

        assertThat(count).hasValue(1)
    }

    @Test
    fun `fold when false`() {
        val count = AtomicInteger(0)
        false.fold({ count.addAndGet(1) }, { count.addAndGet(2) })

        assertThat(count).hasValue(2)
    }
}
