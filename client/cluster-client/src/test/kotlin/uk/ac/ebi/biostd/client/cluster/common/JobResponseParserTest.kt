package uk.ac.ebi.biostd.client.cluster.common

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class JobResponseParserTest {

    private val testInstance: JobResponseParser = JobResponseParser()

    @Test
    fun toJob() {
        val job = testInstance.toJob("Job <9148559> is submitted to default queue <research-rh7>.\n", "/the/logs/path")
        assertThat(job.id).isEqualTo("9148559")
        assertThat(job.queue).isEqualTo("research-rh7")
        assertThat(job.logsPath).isEqualTo("/the/logs/path/9148559_OUT")
    }
}
