package ac.uk.ebi.biostd.common.properties

import uk.ac.ebi.biostd.client.cluster.model.Cluster

data class SubmissionTaskProperties(
    val enabled: Boolean,
    val jarLocation: String,
    val javaLocation: String,
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
    val lsfServer: String,
    val slurmServer: String,
    val logsPath: String,
    val wrapperPath: String,
    val default: Cluster,
)
