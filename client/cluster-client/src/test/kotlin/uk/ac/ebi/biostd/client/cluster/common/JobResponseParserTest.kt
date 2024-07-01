package uk.ac.ebi.biostd.client.cluster.common

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class JobResponseParserTest {
    @Test
    fun toJobLsfResponse() {
        val job = toLsfJob("Job <9148559> is submitted to default queue <research-rh7>.\n")
        assertThat(job.id).isEqualTo("9148559")
        assertThat(job.queue).isEqualTo("research-rh7")
    }

    }
}
