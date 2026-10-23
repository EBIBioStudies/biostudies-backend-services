package uk.ac.ebi.biostd.client.cli.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.createFile
import kotlin.io.path.writeText

internal class DataMoverConfigTest {
    @TempDir
    lateinit var folder: Path

    @Test
    fun `load YAML and resolve SSH key relative to config directory`() {
        val config = loadYaml()

        assertThat(config.sshServer).isEqualTo("biostudies@cluster")
        assertThat(config.sshKey).isEqualTo(folder.resolve("cluster-key"))
        assertThat(config.userFilesRoots).containsEntry("nfs", Path.of("/storage/nfs"))
        assertThat(config.userFilesRoots).containsEntry("ftp", Path.of("/storage/ftp"))
        assertThat(config.jobMinutes).isEqualTo(1440)
        assertThat(config.group).isEqualTo("biostudies")
    }

    @Test
    fun `discover configuration beside JAR independently of working directory`() {
        val jar = folder.resolve("BioStudiesCLI-2.0.jar").createFile()

        assertThat(DataMoverConfig.configFile(jar)).isEqualTo(folder.resolve("biostudies-cli.yaml"))
        assertThat(DataMoverConfig.configFile(folder)).isEqualTo(folder.resolve("biostudies-cli.yaml"))
    }

    @Test
    fun `support explicit job duration and group`() {
        val config = loadYaml("$YAML\njobMinutes: 2880\ngroup: datamovers\n")

        assertThat(config.jobMinutes).isEqualTo(2880)
        assertThat(config.group).isEqualTo("datamovers")
    }

    @Test
    fun `reject missing configuration with its path`() {
        val file = folder.resolve("missing.yaml")

        val error = assertThrows<IllegalArgumentException> { DataMoverConfig.load(file) }

        assertThat(error).hasMessageContaining(file.toString())
    }

    @Test
    fun `reject unknown keys instead of silently accepting typos`() {
        val error = assertThrows<IllegalArgumentException> { loadYaml("$YAML\njobMinute: 60\n") }

        assertThat(error).hasMessageContaining("Unknown configuration keys")
    }

    @Test
    fun `reject duplicate YAML keys`() {
        assertThrows<IllegalArgumentException> { loadYaml("$YAML\nsshServer: another-server\n") }
    }

    @Test
    fun `reject YAML object construction`() {
        assertThrows<IllegalArgumentException> { loadYaml("!!java.util.Date {}") }
    }

    @Test
    fun `reject missing SSH key`() {
        val error = assertThrows<IllegalArgumentException> { loadYaml(YAML.replace("cluster-key", "missing-key")) }

        assertThat(error).hasMessageContaining("SSH key not found")
    }

    @Test
    fun `reject relative cluster paths`() {
        val error = assertThrows<IllegalArgumentException> { loadYaml(YAML.replace("/storage/nfs", "storage/nfs")) }

        assertThat(error).hasMessageContaining("absolute cluster path")
    }

    @Test
    fun `reject nonpositive and noninteger job durations`() {
        assertThrows<IllegalArgumentException> { loadYaml("$YAML\njobMinutes: 0\n") }
        assertThrows<IllegalArgumentException> { loadYaml("$YAML\njobMinutes: 1.5\n") }
    }

    private fun loadYaml(yaml: String = YAML): DataMoverConfig {
        folder.resolve("cluster-key").writeText("test-key")
        val file = folder.resolve("biostudies-cli.yaml")
        file.writeText(yaml)
        return DataMoverConfig.load(file)
    }

    private companion object {
        val YAML =
            """
            sshServer: biostudies@cluster
            sshKey: cluster-key
            wrapperPath: /cluster/wrappers
            logsPath: /cluster/logs
            userFilesRoots:
              nfs: /storage/nfs
              ftp: /storage/ftp
            """.trimIndent()
    }
}
