package ac.uk.ebi.biostd.submission.util

import ebi.ac.uk.model.AccPattern
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PatternProcessorTest {

    private val testInstance = PatternProcessor()

    @Test
    fun getAccPatternWhenPrefix() {
        val prefix = "!{ABC,}"

        val pattern = testInstance.generateAccNumber(prefix) { 10 }
        assertThat(pattern).isInstanceOf(AccPattern::class.java)
        assertThat(pattern.toString()).isEqualTo("ABC10")
    }

    @Test
    fun getAccPatternWhenPostfix() {
        val prefix = "!{,ABC}"

        val pattern = testInstance.generateAccNumber(prefix) { 10 }
        assertThat(pattern).isInstanceOf(AccPattern::class.java)
        assertThat(pattern.toString()).isEqualTo("10ABC")
    }

    @Test
    fun getAccPatternWhenPrefixAndPostfix() {
        val prefix = "!{A,Z}"

        val pattern = testInstance.generateAccNumber(prefix) { 10 }
        assertThat(pattern).isInstanceOf(AccPattern::class.java)
        assertThat(pattern.toString()).isEqualTo("A10Z")
    }
}
