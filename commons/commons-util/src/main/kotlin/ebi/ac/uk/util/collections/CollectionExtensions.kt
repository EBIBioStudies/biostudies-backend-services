package ebi.ac.uk.util.collections

/**
 * Executes the given lambda if the list is not empty.
 *
 * @function lambda function to execute.
 */
fun <T, A : Collection<T>> A.ifNotEmpty(function: (A) -> Unit) {
    if (isNotEmpty()) {
        function(this)
    }
}

/**
 * Creates a list based on the given list and a list of elements.
 *
 * @param otherList the base list to create a new one.
 * @param elements the list of elements to append to the list.
 */
fun <T> merge(
    otherList: List<T>,
    vararg elements: T,
): List<T> = otherList.toMutableList().apply { addAll(elements) }

/**
 * Gets the second element of the list.
 *
 * @throws NoSuchElementException if the list size is less than 2.
 */
fun <T> List<T>.second(): T = if (this.size > 1) this[1] else throw NoSuchElementException("List does not contain a second element.")

/**
 * Gets the third element of the list.
 *
 * @throws NoSuchElementException if the list size is less than 3.
 */
fun <T> List<T>.third(): T = if (this.size > 2) this[2] else throw NoSuchElementException("List does not contain a third element.")

fun <T> List<T>.findSecond(): T? = if (this.size > 1) this[1] else null

fun <T> List<T>.findThird(): T? = if (this.size > 2) this[2] else null

/**
 * Gets the first element of the list or returns the given default value if the element doesn't exist.
 *
 * @param defaultFunc Default lambda to calculate return value if the first element doesn't exist.
 */
fun <T> List<T>.firstOrElse(defaultFunc: () -> T): T = if (isNotEmpty()) this[0] else defaultFunc()

/**
 * Removes and returns the first element of a MutableList and returns it
 */
fun <T> MutableList<T>.removeFirst(): T = removeAt(0)
