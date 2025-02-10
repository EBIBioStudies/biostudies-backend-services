package ebi.ac.uk.coroutines

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SuspendRetryTemplateTest {
    private val testConfig =
        RetryConfig(
            maxAttempts = 2,
            initialInterval = 0,
            multiplier = 0.1,
            maxInterval = 10,
        )
    private val testInstance = SuspendRetryTemplate(testConfig)

    @Test
    fun `happy path`() =
        runTest {
            val result = testInstance.execute("happy path") { "success" }
            assertThat(result).isEqualTo("success")
        }

    @Test
    fun `retry works on second attempt`() =
        runTest {
            var firstAttempt = true

            fun failFirst(): String {
                if (firstAttempt) {
                    firstAttempt = false
                    throw Exception("First attempt fails")
                }

                return "success"
            }

            val result = testInstance.execute("fails first") { failFirst() }
            assertThat(result).isEqualTo("success")
        }
}
