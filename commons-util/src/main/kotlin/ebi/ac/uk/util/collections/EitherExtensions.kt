package ebi.ac.uk.util.collections

import arrow.core.Either
import arrow.core.getOrElse

fun <A, B> MutableList<Either<A, B>>.addLeft(element: A) = add(Either.left(element))

fun <A, B> MutableList<Either<A, B>>.addRight(element: B) = add(Either.right(element))

fun <A, B> MutableList<Either<A, B>>.filterLeft(function: (A) -> Boolean) =
        filterTo(mutableListOf()) { either -> either.isLeft() && function(either.swap().getOrElse { throw Exception() }) }

fun <A, B> Either<A, B>.ifLeft(function: (A) -> Unit) {
    if (isLeft()) {
        function(swap().getOrElse { throw Exception() })
    }
}

fun <A, B> Either<A, B>.ifRight(function: (B) -> Unit) {
    if (isRight()) {
        function(getOrElse { throw Exception() })
    }
}
