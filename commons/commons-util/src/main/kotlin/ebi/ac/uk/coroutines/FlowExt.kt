package ebi.ac.uk.coroutines

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.withIndex

/**
 * Executes the given action every items when flow is collected. Operation is not terminal.
 */
fun <T> Flow<T>.every(
    items: Int,
    action: (IndexedValue<T>) -> Unit,
): Flow<T> {
    return withIndex()
        .transform {
            if (it.index % items == 0) action(it)
            emit(it.value)
        }
}

fun <T, R> Flow<T>.concurrently(
    concurency: Int,
    funtion: suspend (value: T) -> R,
): Flow<R> {
    return flatMapMerge(concurency) { flow { emit(funtion(it)) } }
}
