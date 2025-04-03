package ebi.ac.uk.coroutines

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.withIndex

/**
 * Executes the given action every items when flow is collected. i.e. if items = 10, the elements 1 - 9 will be emited
 * and the action will be executed on element 10 and so on. Operation is not terminal.
 */
fun <T> Flow<T>.every(
    items: Int,
    action: (IndexedValue<T>) -> Unit,
): Flow<T> =
    withIndex()
        .transform {
            if (it.index % items == 0) action(it)
            emit(it.value)
        }

fun <T, R> Flow<T>.concurrently(
    concurrency: Int,
    function: suspend (value: T) -> R,
): Flow<R> = flatMapMerge(concurrency) { flow { emit(function(it)) } }

fun <T, R> Flow<IndexedValue<T>>.concurrentlyIndexed(
    concurrency: Int,
    function: suspend (idx: Int, value: T) -> R,
): Flow<R> = flatMapMerge(concurrency) { flow { emit(function(it.index, it.value)) } }

/**
 * Copy implementation of Kotlin coroutines function until migration 1.9
 */
fun <T> Flow<T>.chunked(size: Int): Flow<List<T>> {
    require(size >= 1) { "Expected positive chunk size, but got $size" }
    return flow {
        var result: ArrayList<T>? = null
        collect { value ->
            val acc = result ?: ArrayList<T>(size).also { result = it }
            acc.add(value)
            if (acc.size == size) {
                emit(acc)
                result = null
            }
        }
        result?.let { emit(it) }
    }
}
