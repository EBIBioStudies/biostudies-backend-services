package uk.ac.ebi.biostd.client.cluster.api

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.ac.ebi.biostd.client.cluster.model.DataMoverQueue
import uk.ac.ebi.biostd.client.cluster.model.JobSpec

internal class SlurmClusterClientTest {
    @Test
    fun `submit command and paths literally without evaluating shell expressions`() {
        val command = "printf '%s' \"${'$'}source\"\nrsync '/source/a\"b' '/target'\n# ${'$'}(echo expanded) `echo expanded`"
        val wrapper = "/wrapper path/it's literal ${'$'}(echo wrong)"
        val logs = "/log path/it's literal `echo wrong`"
        val parameters = with(SlurmClusterClient) { JobSpec(queue = DataMoverQueue, command = command).asParameter(wrapper, logs) }
        val script = "sbatch() { printf '%s\\0' \"${'$'}@\"; }; sbatch --output=/dev/null ${parameters.joinToString(" ")}"
        val process = ProcessBuilder("bash", "-c", script).redirectErrorStream(true).start()
        val arguments = process.inputStream.bufferedReader().readText().split('\u0000').dropLast(1)

        assertThat(process.waitFor()).isZero()
        assertThat(arguments.takeLast(3)).containsExactly("$wrapper/slurm_wrapper.sh", logs, command)
        assertThat(arguments).contains("--partition=datamover")
    }
}
