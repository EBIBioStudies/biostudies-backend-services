package ebi.ac.uk.coroutines

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList

fun <T> Flow<T>.chunked(chunkSize: Int): Flow<List<T>> {
    val buffer = mutableListOf<T>()
    return flow {
        this@chunked.collect {
            buffer.add(it)
            if (buffer.size == chunkSize) {
                emit(buffer.toList())
                buffer.clear()
            }
        }
        if (buffer.isNotEmpty()) {
            emit(buffer.toList())
        }
    }
}

fun <T> allPagesAsFlow(page: Int = 0, limit: Int = 10, function: (Int, Int) -> Flow<T>): Flow<T> {
    return flow {
        var cPage = page
        var result = function(cPage, limit).toList()
        while (result.isNotEmpty()) {
            result.forEach { emit(it) }
            when (result.size) {
                limit -> result = function(++cPage, limit).toList()
                else -> break
            }
        }
    }
}

