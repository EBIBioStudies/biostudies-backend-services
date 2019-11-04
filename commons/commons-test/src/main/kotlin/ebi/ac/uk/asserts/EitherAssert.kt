package ebi.ac.uk.asserts

import arrow.core.Either
import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions.assertThat

fun <A, B> assertThat(option: Either<A, B>): EitherAssert<A, B> {
    return EitherAssert(option)
}

class EitherAssert<A, B>(actual: Either<A, B>) :
    AbstractAssert<EitherAssert<A, B>, Either<A, B>>(actual, EitherAssert::class.java) {

    fun hasLeftValueSatisfying(assertion: (A) -> Unit) {
        assertThat(actual.isLeft())
        actual.fold({ assertion(it) }, {})
    }

    fun hasRightValueSatisfying(assertion: (B) -> Unit) {
        assertThat(actual.isRight())
        actual.fold({ }, { assertion(it) })
    }
}
