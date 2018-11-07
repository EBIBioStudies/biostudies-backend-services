package ebi.ac.uk.util

import arrow.core.Either
import arrow.core.getOrElse

fun <A, B> MutableList<Either<A, B>>.addLeft(element: A) = add(Either.left(element))

fun <A, B> MutableList<Either<A, B>>.addRight(element: B) = add(Either.right(element))

fun <A, B> Either<A, B>.getLeft(): A = swap().getOrElse { throw Exception("It's not a left element") }

fun <A, B> Either<A, B>.getRight(): B = getOrElse { throw Exception("It's not a right element") }
