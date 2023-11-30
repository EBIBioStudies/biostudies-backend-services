package uk.ac.ebi.scheduler.stats.domain

import ac.uk.ebi.scheduler.properties.StatsReporterProperties.Companion.create
import ebi.ac.uk.commons.http.slack.NotificationsSender
import ebi.ac.uk.commons.http.slack.Report
import mu.KotlinLogging
import uk.ac.ebi.biostd.client.cluster.api.ClusterOperations
import uk.ac.ebi.biostd.client.cluster.model.CoresSpec.FOUR_CORES
import uk.ac.ebi.biostd.client.cluster.model.DataMoverQueue
import uk.ac.ebi.biostd.client.cluster.model.Job
import uk.ac.ebi.biostd.client.cluster.model.JobSpec
import uk.ac.ebi.biostd.client.cluster.model.MemorySpec.Companion.EIGHT_GB
import uk.ac.ebi.scheduler.common.SYSTEM_NAME
import uk.ac.ebi.scheduler.common.properties.AppProperties
import uk.ac.ebi.scheduler.stats.api.StatsReporterProperties

private val logger = KotlinLogging.logger {}

class StatsReporterTrigger(
    private val appProperties: AppProperties,
    private val properties: StatsReporterProperties,
    private val clusterOperations: ClusterOperations,
    private val schedulerNotificationsSender: NotificationsSender,
) {
    suspend fun triggerStatsReporter(debugPort: Int? = null): Job {
        logger.info { "Triggering stats reporter job" }
        return triggerJob(debugPort)
    }

    private suspend fun triggerJob(debugPort: Int?): Job {
        val job = statsReporterJob(debugPort)
        schedulerNotificationsSender.send(
            Report(
                SYSTEM_NAME,
                REPORTER_SUBSYSTEM,
                "Triggered $REPORTER_SUBSYSTEM in the cluster job $job. Logs available at ${job.logsPath}"
            )
        )

        return job
    }

    private suspend fun statsReporterJob(debugPort: Int?): Job {
        val properties = getConfigProperties()
        val jobTry = clusterOperations.triggerJobAsync(
            JobSpec(
                cores = FOUR_CORES,
                ram = EIGHT_GB,
                queue = DataMoverQueue,
                command = properties.asCmd(appProperties.appsFolder, debugPort)
            )
        )

        return jobTry.fold({ throw it }, { it.apply { logger.info { "Submitted job $it" } } })
    }

    private fun getConfigProperties() = create(
        properties.persistence.database,
        properties.persistence.uri,
        properties.publishPath,
    )

    companion object {
        const val REPORTER_SUBSYSTEM = "Submission Stats Reporter"
    }
}
