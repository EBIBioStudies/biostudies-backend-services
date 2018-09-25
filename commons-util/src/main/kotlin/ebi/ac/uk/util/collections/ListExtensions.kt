package ebi.ac.uk.util.collections

/**
 * Execute the given lambda is list is not empty.
 *
 * @function lambda function to execute.
 */
fun <A> Collection<A>.ifNotEmpty(function: (Collection<A>) -> Unit) {
    if (this.isNotEmpty()) {
        function(this)
    }
}

fun <T> listFrom(anotherList: List<T>, vararg elements: T): MutableList<T> {
    val newList = anotherList.toMutableList()
    newList.addAll(elements)
    return newList;
}