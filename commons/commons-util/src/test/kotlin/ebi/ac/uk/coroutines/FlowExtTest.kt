package ebi.ac.uk.coroutines

import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class FlowExtTest {

    @Test
    fun chunked() = runTest {
        val source = flowOf(1, 2, 3, 4, 5)

        val result = source
            .chunked(2)
            .toList()

        assertThat(result).hasSize(3)

        val (e1, e2, e3) = result
        assertThat(e1).containsExactly(1, 2)
        assertThat(e2).containsExactly(3, 4)
        assertThat(e3).containsExactly(5)
    }

    @Test
    fun loadAllPagesAsFlow() = runTest {
        val p1 = listOf(1, 2)
        val p2 = listOf(3, 4)
        val p3 = listOf(5)
        val pagedData = listOf(p1, p2, p3)

        val result = allPagesAsFlow(page = 0, limit = 2) { page, limit -> pagedData.get(page).asFlow() }
        assertThat(result.toList()).containsExactly(1, 2, 3, 4, 5)
    }
}
