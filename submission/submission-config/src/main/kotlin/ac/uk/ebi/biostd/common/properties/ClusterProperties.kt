package ac.uk.ebi.biostd.common.properties

data class SubmissionTaskProperties(
    val enabled: Boolean,
    val jarLocation: String,
    val javaLocation: String,
    val configFileLocation: String,
)

data class ClusterProperties(
    val enabled: Boolean,
    val user: String,
    val key: String,
    val server: String,
    val logsPath: String,
)
