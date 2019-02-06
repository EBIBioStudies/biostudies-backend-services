package ebi.ac.uk.asserts

import arrow.core.Either
import arrow.core.getOrElse
import ebi.ac.uk.model.Table
import ebi.ac.uk.util.collections.ifLeft
import ebi.ac.uk.util.collections.ifRight
import org.assertj.core.api.Assertions.assertThat

fun <A, B : Table<A>> assertTable(table: Either<A, B>, vararg expectedRows: A) {
    table.ifRight {
        assertThat(it.elements).hasSize(expectedRows.size)
        it.elements.forEachIndexed { idx, rowElement -> assertThat(rowElement).isEqualTo(expectedRows[idx]) }
    }
}

fun <A, B> assertSingleElement(actual: Either<A, B>, expected: A) = actual.ifLeft { assertThat(it).isEqualTo(expected) }

inline fun <reified A, reified B> Either<A, B>.getLeft() =
    swap().getOrElse { throw AssertionError("Expecting either to have left value of type ${A::class.simpleName}") }

inline fun <reified A, reified B> Either<A, B>.getRight() =
    getOrElse { throw AssertionError("Expecting either to have left value of type ${A::class.simpleName}") }
