package ebi.ac.uk.coroutines

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

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
}
