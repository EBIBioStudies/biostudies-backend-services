package ebi.ac.uk.util.arrow

import arrow.core.Either

fun <A : Any, B, C> List<Either<A, B>>.mapLeft(mapFunc: (A) -> C): List<C> {
    return mapNotNull { either -> either.fold({ it }, { null }) }.map { mapFunc(it) }
}
