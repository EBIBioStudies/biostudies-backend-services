package ebi.ac.uk.util.collections

import java.util.LinkedList

fun <T : Any> List<T>.split(predicate: (T) -> Boolean): List<List<T>> {
    return split(LinkedList(), this, predicate)
}

private tailrec fun <T : Any> split(result: MutableList<List<T>>, input: List<T>, predicate: (T) -> Boolean): List<List<T>> {
    val index = input.indexOfFirst(predicate)
    val head = input.slice(0 until index)
    val tail = input.slice(index + 1 until input.size)

    return when {
        head.isEmpty() && tail.isEmpty() -> result
        head.isEmpty() -> split(result.apply { add(tail) }, tail, predicate)
        else -> split(result.apply { add(head) }, tail, predicate)
    }
}
