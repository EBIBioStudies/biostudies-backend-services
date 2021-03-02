package ebi.ac.uk.util.arrow

import arrow.core.Either

/**
 * Map a list of either into the simple list type by transforming only left side elements, right side are discarded.
 *
 * @return a list of [C] which his the result of computation of left side Eithers in list.
 */
fun <A : Any, B, C> List<Either<A, B>>.mapLeft(mapFunc: (A) -> C): List<C> {
    return mapNotNull { either -> either.fold({ it }, { null }) }.map { mapFunc(it) }
}
