package uk.ac.ebi.scheduler.pmc.exporter.domain

import ac.uk.ebi.cluster.client.lsf.ClusterOperations
import ac.uk.ebi.cluster.client.model.Job
import ac.uk.ebi.cluster.client.model.JobSpec
import ac.uk.ebi.cluster.client.model.MemorySpec.Companion.TWENTYFOUR_GB
import ac.uk.ebi.cluster.client.model.logsPath
import ac.uk.ebi.scheduler.properties.PmcExporterProperties
import ebi.ac.uk.commons.http.slack.NotificationsSender
import ebi.ac.uk.commons.http.slack.Report
import mu.KotlinLogging
import uk.ac.ebi.scheduler.common.SYSTEM_NAME
import uk.ac.ebi.scheduler.common.properties.AppProperties
import uk.ac.ebi.scheduler.pmc.exporter.api.PmcExporterProperties as PmcExporterProps

internal const val EXPORTER_CORES = 4
internal const val PMC_EXPORTER_SUBSYSTEM = "PmcExporter"

private val logger = KotlinLogging.logger {}

internal class PmcExporterTrigger(
    private val appProperties: AppProperties,
    private val clusterOperations: ClusterOperations,
    private val pmcExporterProperties: PmcExporterProps,
    private val notificationsSender: NotificationsSender
) {
    fun triggerPmcExport(): Job {
        logger.info { "triggering pmc export job" }
        val job = pmcExporterJob()
        notificationsSender.send(
            Report(
                SYSTEM_NAME,
                PMC_EXPORTER_SUBSYSTEM,
                "Triggered $PMC_EXPORTER_SUBSYSTEM in the cluster job $job. Logs available at ${job.logsPath}"
            )
        )

        return job
    }

    private fun pmcExporterJob(): Job {
        val pmcExportProperties = getConfigProperties()
        val jobTry = clusterOperations.triggerJob(
            JobSpec(
                cores = EXPORTER_CORES,
                ram = TWENTYFOUR_GB,
                command = pmcExportProperties.asJavaCommand(appProperties.appsFolder)
            )
        )

        return jobTry.fold({ throw it }, { it.apply { logger.info { "submitted job $it" } } })
    }

    private fun getConfigProperties() =
        PmcExporterProperties.create(
            pmcExporterProperties.fileName,
            pmcExporterProperties.outputPath,
            pmcExporterProperties.ftp.host,
            pmcExporterProperties.ftp.user,
            pmcExporterProperties.ftp.password,
            pmcExporterProperties.ftp.port,
            pmcExporterProperties.bioStudies.url,
            pmcExporterProperties.bioStudies.user,
            pmcExporterProperties.bioStudies.password
        )
}
