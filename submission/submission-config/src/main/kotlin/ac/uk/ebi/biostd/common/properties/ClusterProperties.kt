package ac.uk.ebi.biostd.common.properties

data class SubmissionTaskProperties(
    val enabled: Boolean,
    val jarLocation: String,
    val javaLocation: String,
    val javaMemoryAllocation: String,
    val tmpFilesDirPath: String,
    val configFileLocation: String,
    val singleJobMode: Boolean,
    val taskMemoryMgb: Int,
    val taskCores: Int,
    val taskMinutes: Int,
)

data class ClusterProperties(
    val enabled: Boolean,
    val user: String,
    val key: String,
    val slurmServer: String,
    val logsPath: String,
    val wrapperPath: String,
)
