package ebi.ac.uk.util.collections

import arrow.core.Option

/**
 * Executes the given lambda if the list is not empty.
 *
 * @function lambda function to execute.
 */
fun <A : Collection<*>> A.ifNotEmpty(function: (A) -> Unit) {
    if (isNotEmpty()) {
        function(this)
    }
}

/**
 * Creates a list based on the given list and a list of elements.
 *
 * @param anotherList the base list to create new one.
 * @param elements the list of elements to append to the list.
 */
fun <T> merge(anotherList: List<T>, vararg elements: T) = anotherList.toMutableList().apply { addAll(elements) }

/**
 * Obtains the second element of the list.
 *
 * @throws NoSuchElementException if list size is less than 2.
 */
fun <T> List<T>.second() =
    if (this.size > 1) this[1] else throw NoSuchElementException("List does not contain a second element.")

/**
 * Obtains the third element of the list.
 *
 * @throws NoSuchElementException if list size is less than 3.
 */
fun <T> List<T>.third() =
    if (this.size > 2) this[2] else throw NoSuchElementException("List does not contain a third element.")

fun <T> List<T>.findSecond() = if (this.size > 1) Option.just(this[1]) else Option.empty()

fun <T> List<T>.findThird() = if (this.size > 2) Option.just(this[2]) else Option.empty()

/**
 * Obtains the second element of the list or returns the given default value if the element doesn't exist.
 *
 * @param value Default value to return if the second element doesn't exist.
 */
fun <T> List<T>.secondOrElse(value: T) = if (this.size > 1) this[1] else value

/**
 * Obtains the second element of the list or returns the given default value if the element doesn't exist.
 *
 * @param defaultFunc Default lambda to calculate return if the second element doesn't exist.
 */
fun <T> List<T>.secondOrElse(defaultFunc: () -> T) = if (this.size > 1) this[1] else defaultFunc()

/**
 * Obtains the first element of the list or returns the given default value if the element doesn't exist.
 *
 * @param defaultFunc Default lambda to calculate return value if the first element doesn't exist.
 */
fun <T> List<T>.firstOrElse(defaultFunc: () -> T) = if (isNotEmpty()) this[0] else defaultFunc()

/**
 * Removes the first element of a MutableList and returns it
 */
fun <T> MutableList<T>.removeFirst() = removeAt(0)
