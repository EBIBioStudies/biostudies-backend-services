package ac.uk.ebi.biostd.common.properties

data class SubmissionTaskProperties(
    val enabled: Boolean,
    val jarLocation: String,
    val javaLocation: String,
    val configFileLocation: String,
    val taskMemoryMgb: Int,
    val taskCores: Int,
)

data class ClusterProperties(
    val enabled: Boolean,
    val user: String,
    val key: String,
    val lsfServer: String,
    val slurmServer: String,
    val logsPath: String,
    val default: Cluster,
)

enum class Cluster {
    LSF,
    SLURM,
    ;

    companion object {
        fun fromName(name: String): Cluster {
            return when (name.uppercase()) {
                "LSF" -> LSF
                "SLURM" -> SLURM
                else -> throw IllegalArgumentException("$name is not a valid cluster name")
            }
        }
    }
}
