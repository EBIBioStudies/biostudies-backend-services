package ac.uk.ebi.pmc.common.coroutines

/**
 * Allow the subscription to a suspend sequence of element of type T, sequence is consider finalized when null element
 * is found.
 */
class SuspendSequence<T>(private val read: suspend () -> T?) {

    private var next: T? = null
    private var done = false

    suspend fun forEach(function: suspend (T) -> Any) {
        while (hasNext()) function(next())
    }

    @Suppress("ReturnCount")
    private suspend fun hasNext(): Boolean {
        if (done) return false

        if (next == null) {
            next = read()
            if (next == null) {
                done = true
                return false
            }
        }

        return true
    }

    private fun next(): T {
        return next!!.also { next = null }
    }
}
