package uk.ac.ebi.biostd.client.cluster.common

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Test

internal class JobResponseParserTest {
    @Test
    fun toJobLsfResponse() {
        val job = toLsfJob("Job <9148559> is submitted to default queue <research-rh7>.\n")
        assertThat(job.id).isEqualTo("9148559")
        assertThat(job.queue).isEqualTo("research-rh7")
    }

    @Test
    fun toJobLsfResponseWhenSpecificQueue() {
        val job = toLsfJob("Job <4838563> is submitted to queue <standard>.")
        assertThat(job.id).isEqualTo("4838563")
        assertThat(job.queue).isEqualTo("standard")
    }

    @Test
    fun toJobLsfWhenError() {
        assertThatExceptionOfType(IllegalStateException::class.java)
            .isThrownBy { toLsfJob("Job Could not be submitted") }
            .withMessage("could not parse response, 'Job Could not be submitted'")
    }

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
