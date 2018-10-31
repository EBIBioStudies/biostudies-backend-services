package ebi.ac.uk.util.collections

/**
 * Executes the given lambda if the list is not empty.
 *
 * @function lambda function to execute.
 */
fun <A : Collection<*>> A.ifNotEmpty(function: (A) -> Unit) {
    if (this.isNotEmpty()) {
        function(this)
    }
}

/**
 * Creates a list based on the given list and a list of attributes.
 *
 * @param anotherList the base list to create new one.
 * @param elements the list of elements to append to the list.
 */
fun <T> listFrom(anotherList: List<T>, vararg elements: T): List<T> {
    val newList = anotherList.toMutableList()
    newList.addAll(elements)
    return newList
}

/**
 * Obtain the second element of the list.
 *
 * @throws NoSuchElementException if list size is less than 2.
 */
fun <T> List<T>.second(): T {
    if (this.size < 2)
        throw NoSuchElementException("List do not contain a second element.")
    return this[1]
}

/**
 * Removes the first element of a MutableList and returns it
 */
fun <T> MutableList<T>.removeFirst() = removeAt(0)
