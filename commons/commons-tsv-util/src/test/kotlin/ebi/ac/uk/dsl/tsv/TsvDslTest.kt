package ebi.ac.uk.dsl.tsv

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

val tsvTest =
    tsv {
        line("That's")
        line("tsv", "test")
        line()
    }

internal class TsvDslTest {
    @Test
    fun toStringTest() {
        assertThat(tsvTest.toString()).isEqualTo("That's\ntsv\ttest\n")
    }
}
