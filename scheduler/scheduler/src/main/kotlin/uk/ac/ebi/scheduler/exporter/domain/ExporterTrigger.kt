package uk.ac.ebi.scheduler.exporter.domain

import ac.uk.ebi.cluster.client.lsf.ClusterOperations
import ac.uk.ebi.cluster.client.model.Job
import ac.uk.ebi.cluster.client.model.JobSpec
import ac.uk.ebi.cluster.client.model.MemorySpec.Companion.EIGHT_GB
import ac.uk.ebi.cluster.client.model.logsPath
import ac.uk.ebi.scheduler.properties.ExporterProperties
import ebi.ac.uk.commons.http.slack.NotificationsSender
import ebi.ac.uk.commons.http.slack.Report
import mu.KotlinLogging
import uk.ac.ebi.scheduler.common.SYSTEM_NAME
import uk.ac.ebi.scheduler.common.properties.AppProperties
import uk.ac.ebi.scheduler.exporter.api.ExporterProperties as ExporterProps

internal const val EXPORTER_CORES = 4
internal const val EXPORTER_SUBSYSTEM = "Exporter"

private val logger = KotlinLogging.logger {}

class ExporterTrigger(
    private val appProperties: AppProperties,
    private val exporterProperties: ExporterProps,
    private val clusterOperations: ClusterOperations,
    private val notificationsSender: NotificationsSender
) {
    fun triggerPublicExport(): Job {
        logger.info { "triggering public export job" }
        val job = exporterJob()
        notificationsSender.send(
            Report(
            SYSTEM_NAME,
            EXPORTER_SUBSYSTEM,
            "Triggered $EXPORTER_SUBSYSTEM in the cluster job $job. Logs available at ${job.logsPath}"))

        return job
    }

    private fun exporterJob(): Job {
        val exporterProperties = getConfigProperties()
        val jobTry = clusterOperations.triggerJob(
            JobSpec(
                cores = EXPORTER_CORES,
                ram = EIGHT_GB,
                command = exporterProperties.asJavaCommand(appProperties.appsFolder)))

        return jobTry.fold({ throw it }, { it.apply { logger.info { "submitted job $it" } } })
    }

    private fun getConfigProperties() =
        ExporterProperties.create(
            exporterProperties.fileName,
            exporterProperties.outputPath,
            exporterProperties.bioStudies.url,
            exporterProperties.bioStudies.user,
            exporterProperties.bioStudies.password
        )
}
