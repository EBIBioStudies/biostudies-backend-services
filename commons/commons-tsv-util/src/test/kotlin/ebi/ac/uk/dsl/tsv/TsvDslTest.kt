package ebi.ac.uk.dsl.tsv

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

val tsvTest = tsv {
    line("Submission")
    line("Title", "Excel Submission")
    line()
}

internal class TsvDslTest {
    @Test
    fun toStringTest() {
        assertThat(tsvTest.toString()).isEqualTo("Submission\nTitle\tExcel Submission\n")
    }
}
