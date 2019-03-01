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
