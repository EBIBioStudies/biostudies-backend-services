package uk.ac.ebi.scheduler.pmc.exporter.domain

import ac.uk.ebi.scheduler.properties.ExporterMode
import ac.uk.ebi.scheduler.properties.ExporterMode.PMC
import ac.uk.ebi.scheduler.properties.ExporterMode.PUBLIC_ONLY
import ac.uk.ebi.scheduler.properties.ExporterProperties
import ebi.ac.uk.commons.http.slack.NotificationsSender
import ebi.ac.uk.commons.http.slack.Report
import mu.KotlinLogging
import uk.ac.ebi.biostd.client.cluster.api.ClusterClient
import uk.ac.ebi.biostd.client.cluster.model.Job
import uk.ac.ebi.biostd.client.cluster.model.JobSpec
import uk.ac.ebi.biostd.client.cluster.model.MemorySpec.Companion.TWENTY_FOUR_GB
import uk.ac.ebi.scheduler.common.SYSTEM_NAME
import uk.ac.ebi.scheduler.common.properties.AppProperties
import uk.ac.ebi.scheduler.pmc.exporter.api.ExporterProperties as ExporterProps

internal const val EXPORTER_CORES = 4
internal const val EXPORTER_SUBSYSTEM = "Exporter"

private val logger = KotlinLogging.logger {}

class ExporterTrigger(
    private val appProperties: AppProperties,
    private val exporterProperties: ExporterProps,
    private val clusterClient: ClusterClient,
    private val pmcNotificationsSender: NotificationsSender,
    private val schedulerNotificationsSender: NotificationsSender,
) {
    suspend fun triggerPmcExport(debugPort: Int? = null): Job {
        logger.info { "Triggering PMC export job" }

        val config =
            ExporterJobConfig(
                PMC,
                exporterProperties.pmc.fileName,
                exporterProperties.pmc.outputPath,
                debugPort,
                pmcNotificationsSender,
            )

        return triggerExport(config)
    }

    suspend fun triggerPublicExport(debugPort: Int? = null): Job {
        logger.info { "Triggering public only export job" }

        val config =
            ExporterJobConfig(
                PUBLIC_ONLY,
                exporterProperties.publicOnly.fileName,
                exporterProperties.publicOnly.outputPath,
                debugPort,
                schedulerNotificationsSender,
            )

        return triggerExport(config)
    }

    private suspend fun triggerExport(config: ExporterJobConfig): Job {
        val mode = config.mode
        val job = exporterJob(config)
        config.notifier.send(
            Report(
                SYSTEM_NAME,
                EXPORTER_SUBSYSTEM,
                "Triggered $EXPORTER_SUBSYSTEM in the cluster job $job in mode $mode.",
            ),
        )

        return job
    }

    private suspend fun exporterJob(config: ExporterJobConfig): Job {
        val (mode, fileName, outputPath, debugPort, _) = config
        val exporterProperties = getConfigProperties(mode, fileName, outputPath)
        val cmd = exporterProperties.asCmd(appProperties.appsFolder, debugPort)
        val jobTry = clusterClient.triggerJobAsync(JobSpec(cores = EXPORTER_CORES, ram = TWENTY_FOUR_GB, command = cmd))
        return jobTry.fold({ it.apply { logger.info { "submitted job $it" } } }, { throw it })
    }

    private fun getConfigProperties(
        mode: ExporterMode,
        fileName: String,
        outputPath: String,
    ) = ExporterProperties.create(
        fileName,
        outputPath,
        exporterProperties.tmpFilesPath,
        mode,
        exporterProperties.ftp.host,
        exporterProperties.ftp.user,
        exporterProperties.ftp.password,
        exporterProperties.ftp.port,
        exporterProperties.persistence.database,
        exporterProperties.persistence.uri,
        exporterProperties.bioStudies.url,
        exporterProperties.bioStudies.user,
        exporterProperties.bioStudies.password,
    )

    private data class ExporterJobConfig(
        val mode: ExporterMode,
        val fileName: String,
        val outputPath: String,
        val debugPort: Int?,
        val notifier: NotificationsSender,
    )
}
