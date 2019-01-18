package ac.uk.ebi.cluster.client.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class JobSpecTest {

    @Test
    fun asParameter() {
        val params = JobSpec(1, MemorySpec.SIXTEEN_GB, "hostname").asParameter()
        assertThat(params).containsExactly("-n", "1", "-M", "16384", "-R", "rusage[mem=16384]", "hostname")
    }
}
