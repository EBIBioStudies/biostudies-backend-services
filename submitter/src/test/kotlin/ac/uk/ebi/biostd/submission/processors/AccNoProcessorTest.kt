package ac.uk.ebi.biostd.submission.processors

import ac.uk.ebi.biostd.submission.util.AccNumber
import ac.uk.ebi.biostd.submission.util.PrefixPostfix
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class AccNoProcessorTest {

    private val testInstance = AccNoProcessor()

    @ParameterizedTest(name = "when prefix is {0}, postfix is {1} and numeric value is {2}")
    @CsvSource(
            "AA, BB, 88, AA/AA0-99BB/AA88BB",
            "AA, BB, 200, AA/AAxxx200BB/AA200BB",
            "AA, '', 88, AA/AA0-99/AA88",
            "AA, '', 200, AA/AAxxx200/AA200",
            "'', 'BB', 88, 0-99BB/88BB",
            "'', 'BB', 200, xxx200BB/200BB"
    )
    fun getRootPath(prefix: String, postfix: String, value: Long, expected: String) {
        assertThat(testInstance.getRelPath(AccNumber(PrefixPostfix(prefix, postfix), value))).isEqualTo(expected)
    }
}
