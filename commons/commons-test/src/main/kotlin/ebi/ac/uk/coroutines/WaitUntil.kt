package ebi.ac.uk.coroutines

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.time.Duration

@Suppress("MagicNumber")
suspend fun waitUntil(
    duration: Duration,
    interval: Duration = Duration.ofMillis(300),
    conditionEvaluator: suspend () -> Boolean,
) {

    /**
     * Wait until the given condition is complete. Function use specific context as test context generated by runTest
     * function does not wait for delay operation as "time" move faster.
     */
    suspend fun waitUntil(
        conditionEvaluator: suspend () -> Boolean,
        available: Long,
        interval: Long,
    ): Unit = withContext(Dispatchers.Default) {
        if (available < interval) throw IllegalArgumentException("Await condition expired")
        val result = runCatching { conditionEvaluator() }.getOrElse { false }
        if (result.not()) {
            delay(interval)
            waitUntil(conditionEvaluator, available - interval, interval)
        }
    }

    waitUntil(conditionEvaluator, duration.toMillis(), interval.toMillis())
}