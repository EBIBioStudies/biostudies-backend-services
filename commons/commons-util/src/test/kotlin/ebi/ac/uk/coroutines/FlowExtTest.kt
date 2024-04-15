package ebi.ac.uk.coroutines

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.measureTimeMillis

class FlowExtTest {
    @Test
    fun every() =
        runTest {
            var count = 0
            flowOf(1, 2, 3, 4)
                .every(2) { count++ }
                .collect()

            assertThat(count).isEqualTo(2)
        }

    @Test
    fun everyIsNotTerminal() =
        runTest {
            var count = 0
            flowOf(1, 2, 3).every(1) { count++ }

            assertThat(count).isEqualTo(0)
        }

    @Test
    fun concurrentlyIsConcurrent(): Unit =
        runBlocking {
            suspend fun asyncFunction(element: Int): String {
                delay(100)
                return "Processed element $element"
            }

            val concurrency = 5
            val input = (1..10).asFlow()

            val totalTime =
                measureTimeMillis {
                    input
                        .concurrently(concurrency) { asyncFunction(it) }
                        .collect()
                }

            assertThat(totalTime).isLessThan(300)
        }

    @Test
    fun concurrentlyIsCorrect(): Unit =
        runTest {
            var value = AtomicInteger(0)
            val concurrency = 5
            val input = (1..10).asFlow()

            input
                .concurrently(concurrency) { value.getAndIncrement() }
                .collect()

            assertThat(value).hasValue(10)
        }
}
