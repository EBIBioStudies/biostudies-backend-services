package ac.uk.ebi.biostd.common.properties

import uk.ac.ebi.biostd.client.cluster.model.Cluster

data class SubmissionTaskProperties(
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
    val wrapperPath: String,
    val default: Cluster,
)
