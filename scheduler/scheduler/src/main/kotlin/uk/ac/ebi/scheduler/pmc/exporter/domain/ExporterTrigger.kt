package uk.ac.ebi.scheduler.pmc.exporter.domain

import ac.uk.ebi.cluster.client.lsf.ClusterOperations
import ac.uk.ebi.cluster.client.model.Job
import ac.uk.ebi.cluster.client.model.JobSpec
import ac.uk.ebi.cluster.client.model.MemorySpec.Companion.TWENTYFOUR_GB
import ac.uk.ebi.cluster.client.model.logsPath
import ac.uk.ebi.scheduler.properties.ExporterMode
import ac.uk.ebi.scheduler.properties.ExporterMode.PMC
import ac.uk.ebi.scheduler.properties.ExporterMode.PUBLIC_ONLY
import ac.uk.ebi.scheduler.properties.ExporterProperties
import ebi.ac.uk.commons.http.slack.NotificationsSender
import ebi.ac.uk.commons.http.slack.Report
import mu.KotlinLogging
import uk.ac.ebi.scheduler.common.SYSTEM_NAME
import uk.ac.ebi.scheduler.common.properties.AppProperties
import uk.ac.ebi.scheduler.pmc.exporter.api.ExporterProperties as ExporterProps

internal const val EXPORTER_CORES = 4
internal const val EXPORTER_SUBSYSTEM = "Exporter"

private val logger = KotlinLogging.logger {}

class ExporterTrigger(
    private val appProperties: AppProperties,
    private val exporterProperties: ExporterProps,
    private val clusterOperations: ClusterOperations,
    private val notificationsSender: NotificationsSender,
) {
    fun triggerPmcExport(debugPort: Int? = null): Job {
        logger.info { "Triggering PMC export job" }

        return triggerExport(
            PMC,
            exporterProperties.pmc.fileName,
            exporterProperties.pmc.outputPath,
            debugPort
        )
    }

    fun triggerPublicExport(debugPort: Int? = null): Job {
        logger.info { "Triggering public only export job" }

        return triggerExport(
            PUBLIC_ONLY,
            exporterProperties.publicOnly.fileName,
            exporterProperties.publicOnly.outputPath,
            debugPort
        )
    }

    private fun triggerExport(mode: ExporterMode, fileName: String, outputPath: String, debugPort: Int?): Job {
        val job = exporterJob(mode, fileName, outputPath, debugPort)
        notificationsSender.send(
            Report(
                SYSTEM_NAME,
                EXPORTER_SUBSYSTEM,
                "Triggered $EXPORTER_SUBSYSTEM in the cluster job $job in mode $mode. Logs available at ${job.logsPath}"
            )
        )

        return job
    }

    private fun exporterJob(mode: ExporterMode, fileName: String, outputPath: String, debugPort: Int?): Job {
        val exporterProperties = getConfigProperties(mode, fileName, outputPath)
        val cmd = exporterProperties.asCmd(appProperties.appsFolder, debugPort)
        val jobTry = clusterOperations.triggerJob(JobSpec(cores = EXPORTER_CORES, ram = TWENTYFOUR_GB, command = cmd))
        return jobTry.fold({ throw it }, { it.apply { logger.info { "submitted job $it" } } })
    }

    private fun getConfigProperties(mode: ExporterMode, fileName: String, outputPath: String) =
        ExporterProperties.create(
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
            exporterProperties.bioStudies.password
        )
}
