package ebi.ac.uk.base

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class BooleanExtensionsTest {

    @Test
    fun orFalseWhenNotNullable() {
        assertThat(false.orFalse()).isEqualTo(false)
        assertThat(true.orFalse()).isEqualTo(true)
    }

    @Test
    fun orFalseWhenNullable() {
        val nullable: Boolean? = false
        assertThat(nullable).isEqualTo(false)
    }

    @Test
    fun whenTrueWhenTrue() {
        var count = 0
        true.ifTrue { count++ }

        assertThat(count).isEqualTo(1)
    }

    @Test
    fun whenTrueWhenFalse() {
        var count = 0
        false.ifTrue { count++ }

        assertThat(count).isEqualTo(0)
    }
}
