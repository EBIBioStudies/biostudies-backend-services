package ac.uk.ebi.pmc.common.coroutines

/**
 * Allow the subscription to a suspend sequence of element of type T, sequence is consider finalized when null element
 * is found.
 */
class SuspendSequence<T>(
    private val limit: Int? = null,
    private val read: suspend () -> T?
) {

    private var next: T? = null
    private var done = false
    private var records: Int = 0

    suspend fun forEach(function: suspend (T) -> Any) {
        while (hasNext()) function(next())
    }

    @Suppress("ReturnCount")
    private suspend fun hasNext(): Boolean {
        if (done || records == limit) return false

        if (next == null) {
            next = read()
            if (next == null) {
                done = true
                return false
            }

            records++
        }

        return true
    }

    private fun next(): T {
        return next!!.also { next = null }
    }
}
