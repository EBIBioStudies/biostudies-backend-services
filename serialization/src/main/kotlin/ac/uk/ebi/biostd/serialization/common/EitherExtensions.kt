package ac.uk.ebi.biostd.serialization.common

import arrow.core.Either

fun <A, B> MutableList<Either<A, B>>.addLeft(element: A) = add(Either.left(element))

fun <A, B> MutableList<Either<A, B>>.addRight(element: B) = add(Either.right(element))

