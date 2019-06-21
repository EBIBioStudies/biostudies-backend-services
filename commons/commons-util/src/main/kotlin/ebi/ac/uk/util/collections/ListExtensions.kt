package ebi.ac.uk.util.collections

import java.util.LinkedList

private const val NO_FOUND = -1

inline fun <T> List<T>.indexOf(predicate: (T) -> Boolean): Int? {
    for ((index, item) in this.withIndex()) {
        if (predicate(item)) return index
    }

    return null
}

/**
 * Split the list into a list of list using the current predicate true condition. Note that elements when predicate is
 * true are not included in the result lists.
 */
fun <T : Any> List<T>.split(predicate: (T) -> Boolean): List<List<T>> = split(LinkedList(), this, predicate)

private tailrec fun <T : Any> split(
    result: MutableList<List<T>>,
    input: List<T>,
    predicate: (T) -> Boolean
): List<List<T>> {
    val index = input.indexOfFirst(predicate)
    val head = input.slice(0 until index)
    val tail = input.slice(index + 1 until input.size)

    return when {
        head.isEmpty() && tail.isEmpty() -> result
        index == NO_FOUND -> result.apply { add(tail) }
        head.isEmpty() -> split(result, tail, predicate)
        else -> split(result.apply { add(head) }, tail, predicate)
    }
}
