package ebi.ac.uk.util.collections

import arrow.core.Either
import arrow.core.getOrElse

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
 * Filters a list of Either elements leaving only the elements that meets these conditions:
 * 1) The element is a left element
 * 2) The given function returns true for the element
 *
 * @param function Boolean function to determine whether the element should be on the list
 */
fun <A, B> MutableList<Either<A, B>>.filterLeft(function: (A) -> Boolean) =
        filterTo(mutableListOf()) { either -> either.isLeft() && function(either.getLeft()) }

/**
 * Applies the given function over the element ONLY if it's a left element, otherwise it does nothing.
 *
 * @param function The function to apply over the element
 */
fun <A, B> Either<A, B>.ifLeft(function: (A) -> Unit) {
    if (isLeft()) {
        function(getLeft())
    }
}

/**
 * Applies the given function over the element ONLY if it's a right element, otherwise it does nothing.
 *
 * @param function The function to apply over the element
 */
fun <A, B> Either<A, B>.ifRight(function: (B) -> Unit) {
    if (isRight()) {
        function(getOrElse { throw Exception() })
    }
}

/**
 * Gets the value for a Left element.
 *
 * @throws Exception if the element is not Left.
 */
private fun <A, B> Either<A, B>.getLeft(): A = swap().getOrElse { throw Exception("Not a left element") }
