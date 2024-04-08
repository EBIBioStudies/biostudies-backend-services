package uk.ac.ebi.biostd.client.cluster.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.ac.ebi.biostd.client.cluster.model.MemorySpec.Companion.SIXTEEN_GB

internal class JobSpecTest {
    @Test
    fun asParameter() {
        val params = JobSpec(1, SIXTEEN_GB, DataMoverQueue, "hostname").asParameter()
        assertThat(params).containsExactly(
            "-n",
            "1",
            "-M",
            "16384",
            "-R",
            "rusage[mem=16384]",
            "-q",
            "datamover",
            "hostname",
        )
    }
}
