package ebi.ac.uk.util.collections

import arrow.core.Either

/**
 * Adds a { @Link Either#left } to a mutable list of Either containing the given element.
 *
 * @param element The element to add to the list
 */
fun <A, B> MutableList<Either<A, B>>.addLeft(element: A) = add(Either.left(element))

/**
 * Adds a { @Link Either.right } to a mutable list of Either containing the given element.
 *
 * @param element The element to add to the list
 */
fun <A, B> MutableList<Either<A, B>>.addRight(element: B) = add(Either.right(element))

/**
 * Applies the given function over the element ONLY if it's a left element, otherwise it does nothing.
 *
 * @param function The function to apply over the element
 */
fun <A, B> Either<A, B>.ifLeft(function: (A) -> Unit) = fold(function, {})

/**
 * Applies the given function over the element ONLY if it's a right element, otherwise it does nothing.
 *
 * @param function The function to apply over the element
 */
fun <A, B> Either<A, B>.ifRight(function: (B) -> Unit) = fold({}, function)

/**
 * Reduce the list of either into the simple list type by transforming only left side elements, right side are
 * discarded.
 *
 * @return a list of [C] which his the result of computation of left side Eithers in list.
 */
fun <A : Any, B, C> List<Either<A, B>>.reduceLeft(mapFunc: (A) -> C): List<C> =
    mapNotNull { either -> either.fold({ it }, { null }) }.map { mapFunc(it) }

/**
 * Map left values of a list of either.
 *
 * @return a list of [C] which his the result of computation of left side Eithers in list.
 */
fun <A : Any, B, C> List<Either<A, B>>.mapLeft(mapFunc: (A) -> C): List<Either<C, B>> = map { it.mapLeft(mapFunc) }
