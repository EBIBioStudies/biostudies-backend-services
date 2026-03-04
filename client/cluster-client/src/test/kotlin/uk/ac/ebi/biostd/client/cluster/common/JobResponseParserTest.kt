package uk.ac.ebi.biostd.client.cluster.common

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Test

internal class JobResponseParserTest {

    @Test
    fun toJobSlurmRespone() {
        val job = toSlurmJob("Submitted batch job 27223401", "logs/path")
        assertThat(job.id).isEqualTo("27223401")
        assertThat(job.queue).isEqualTo("not-specified")
        assertThat(job.logsPath).isEqualTo("logs/path/401/27223401_OUT")
    }

    @Test
    fun toJobSlurmResponeWhenError() {
        assertThatExceptionOfType(IllegalStateException::class.java)
            .isThrownBy { toSlurmJob("Job Could not be submitted", "logs/path") }
            .withMessage("could not parse response, 'Job Could not be submitted'")
    }
}
