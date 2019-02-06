package ebi.ac.uk.base

import arrow.core.Option
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.Optional

internal class OptionalExtTest {

    @Test
    fun toOptionWhenEmpty() {
        assertThat(Optional.empty<String>().toOption()).isEqualTo(Option.empty<String>())
    }

    @Test
    fun toOptionWhenValue() {
        assertThat(Optional.of("content").toOption()).isEqualTo(Option.just("content"))
    }
}
