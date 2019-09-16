package ebi.ac.uk.asserts

import arrow.core.Option
import arrow.core.getOrElse
import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.internal.Failures

fun <T> assertThat(option: Option<T>): OptionAssert<T> {
    return OptionAssert(option)
}

class OptionAssert<T>(actual: Option<T>) :
    AbstractAssert<OptionAssert<T>, Option<*>>(actual, OptionAssert::class.java) {

    fun contains(value: T) {
        val optionValue = actual.getOrElse { Failures.instance().failure("Expecting option to contain a value") }
        assertThat(optionValue).isEqualTo(value)
    }

    fun isEmpty() {
        assertThat(actual.isEmpty()).isTrue()
    }
}
