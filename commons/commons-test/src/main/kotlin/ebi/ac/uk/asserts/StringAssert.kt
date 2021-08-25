package ebi.ac.uk.asserts

import ebi.ac.uk.base.remove
import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions.assertThat

private val LINE_BREAK_SPACES_REGEX = "[\\n\\r\\s]+".toRegex()

object StringAssertion {
    fun assertThat(value: String): StringAssert = StringAssert(value)
}

class StringAssert(actual: String) : AbstractAssert<StringAssert, String>(actual, StringAssert::class.java) {
    fun isEqualsIgnoringSpacesAndLineBreaks(value: String) {
        assertThat(actual.remove(LINE_BREAK_SPACES_REGEX)).isEqualTo(value.remove(LINE_BREAK_SPACES_REGEX))
    }
}
