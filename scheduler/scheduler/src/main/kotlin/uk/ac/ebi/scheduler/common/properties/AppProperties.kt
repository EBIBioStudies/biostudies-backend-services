package uk.ac.ebi.scheduler.common.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import uk.ac.ebi.biostd.client.cluster.model.Cluster

@ConfigurationProperties(prefix = "app")
data class AppProperties(
    val appsFolder: String,
    val javaHome: String,
    val slack: SlackConfiguration,
    val dailyScheduling: DailyScheduling,
    val cluster: ClusterConfiguration,
)

data class DailyScheduling(
    val pmcImport: Boolean = false,
    val pmcExport: Boolean = false,
    val notifier: Boolean = false,
    val releaser: Boolean = true,
    val exporter: Boolean = true,
    val statsReporter: Boolean = true,
)

data class SlackConfiguration(
    val pmcNotificationsUrl: String,
    val schedulerNotificationsUrl: String,
)

data class ClusterConfiguration(
    val user: String,
    val sshKey: String,
    val lsfServer: String,
    val slurmServer: String,
    val logsPath: String,
    val wrapperPath: String,
    val default: Cluster,
)
