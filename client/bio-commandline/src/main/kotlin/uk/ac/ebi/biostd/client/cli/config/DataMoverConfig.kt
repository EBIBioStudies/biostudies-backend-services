package uk.ac.ebi.biostd.client.cli.config

import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.SafeConstructor
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isDirectory

internal data class DataMoverConfig(
    val sshServer: String,
    val sshKey: Path,
    val wrapperPath: String,
    val logsPath: String,
    val userFilesRoots: Map<String, Path>,
    val jobMinutes: Int = DEFAULT_JOB_MINUTES,
    val group: String = "biostudies",
) {
    init {
        require(sshServer.isNotBlank()) { "sshServer must not be blank" }
        require(Path.of(wrapperPath).isAbsolute) { "wrapperPath must be an absolute cluster path" }
        require(Path.of(logsPath).isAbsolute) { "logsPath must be an absolute cluster path" }
        require(jobMinutes > 0) { "jobMinutes must be positive" }
        require(group.isNotBlank()) { "group must not be blank" }
        require(userFilesRoots.isNotEmpty()) { "userFilesRoots must specify an nfs or ftp root" }
        userFilesRoots.forEach { (type, path) ->
            require(type in setOf("nfs", "ftp")) { "Unsupported upload type in userFilesRoots: $type" }
            require(path.isAbsolute) { "userFilesRoots.$type must be an absolute cluster path" }
        }
    }

    companion object {
        private const val DEFAULT_JOB_MINUTES = 1440
        private const val CONFIG_FILE_NAME = "biostudies-cli.yaml"
        private val CONFIG_KEYS = setOf("sshServer", "sshKey", "wrapperPath", "logsPath", "userFilesRoots", "jobMinutes", "group")

        fun load(): DataMoverConfig {
            val location = Path.of(DataMoverConfig::class.java.protectionDomain.codeSource.location.toURI())
            return load(configFile(location))
        }

        internal fun configFile(location: Path): Path =
            (if (location.isDirectory()) location else location.parent).resolve(CONFIG_FILE_NAME)

        internal fun load(file: Path): DataMoverConfig {
            require(Files.isRegularFile(file) && Files.isReadable(file)) { "Datamover configuration not found or unreadable: $file" }
            return runCatching {
                val options = LoaderOptions().apply { isAllowDuplicateKeys = false }
                val config = Files.newBufferedReader(file).use { Yaml(SafeConstructor(options)).load<Any>(it) }
                parse(config, file.toAbsolutePath().parent)
            }.getOrElse { error ->
                throw IllegalArgumentException("Invalid datamover configuration at $file: ${error.message}", error)
            }
        }

        private fun parse(
            value: Any?,
            configDirectory: Path,
        ): DataMoverConfig {
            val values = value as? Map<*, *> ?: error("Expected a YAML mapping")
            require(values.keys.all { it in CONFIG_KEYS }) { "Unknown configuration keys: ${values.keys - CONFIG_KEYS}" }
            val roots = values["userFilesRoots"] as? Map<*, *> ?: error("userFilesRoots must be a mapping")
            val paths =
                roots.entries.associate { (type, path) ->
                    require(type is String && path is String && path.isNotBlank()) { "userFilesRoots must map storage types to paths" }
                    type to Path.of(path).normalize()
                }
            val key = configDirectory.resolve(values.requiredString("sshKey")).normalize()
            require(Files.isRegularFile(key) && Files.isReadable(key)) { "SSH key not found or unreadable: $key" }
            val minutes = values["jobMinutes"] ?: DEFAULT_JOB_MINUTES
            require(minutes is Int) { "jobMinutes must be an integer" }
            return DataMoverConfig(
                sshServer = values.requiredString("sshServer"),
                sshKey = key,
                wrapperPath = values.requiredString("wrapperPath"),
                logsPath = values.requiredString("logsPath"),
                userFilesRoots = paths,
                jobMinutes = minutes,
                group = if ("group" in values) values.requiredString("group") else "biostudies",
            )
        }

        private fun Map<*, *>.requiredString(name: String): String {
            val value = get(name)
            require(value is String && value.isNotBlank()) { "$name must be a non-empty string" }
            require('\u0000' !in value) { "$name must not contain a NUL character" }
            return value
        }
    }
}
