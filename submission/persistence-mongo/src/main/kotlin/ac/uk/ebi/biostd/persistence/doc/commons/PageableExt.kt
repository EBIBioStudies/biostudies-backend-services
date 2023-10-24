package ac.uk.ebi.biostd.persistence.doc.commons

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList

fun <T> pageResultAsFlow(page: Int = 0, limit: Int = 10, function: (Int, Int) -> Flow<T>): Flow<T> {
    return flow {
        var cPage = page
        var result = function(cPage, limit).toList()
        while (result.isNotEmpty()) {
            result.forEach { emit(it) }
            result = function(++cPage, limit).toList()
        }
    }
}
