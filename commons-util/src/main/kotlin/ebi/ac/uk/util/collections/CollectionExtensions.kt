package ebi.ac.uk.util.collections

/**
 * Execute the given lambda is list is not empty.
 *
 * @function lambda function to execute.
 */
fun <A : Collection<*>> A.ifNotEmpty(function: (A) -> Unit) {
    if (this.isNotEmpty()) {
        function(this)
    }
}

/**
 * Create a list based on the given list an a list of attributes.
 *
 * @param anotherList the base list to create new one.
 * @param elements the list of elements to append to the list.
 */
fun <T> listFrom(anotherList: List<T>, vararg elements: T): List<T> {
    val newList = anotherList.toMutableList()
    newList.addAll(elements)
    return newList
}

fun <T> List<T>.second(): T {
    if (this.size < 2)
        throw NoSuchElementException("List do not contain a second element.")
    return this[1]
}

fun <T> MutableList<T>.removeFirst() = removeAt(0)
