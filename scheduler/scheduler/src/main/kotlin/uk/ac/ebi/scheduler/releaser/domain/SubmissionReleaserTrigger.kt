package uk.ac.ebi.scheduler.releaser.domain

import ac.uk.ebi.cluster.client.lsf.ClusterOperations
import ac.uk.ebi.cluster.client.model.Job
import ac.uk.ebi.cluster.client.model.JobSpec
import ac.uk.ebi.cluster.client.model.MemorySpec
import ac.uk.ebi.cluster.client.model.logsPath
import ac.uk.ebi.scheduler.properties.ReleaserMode
import ac.uk.ebi.scheduler.properties.ReleaserMode.NOTIFY
import ac.uk.ebi.scheduler.properties.ReleaserMode.RELEASE
import ac.uk.ebi.scheduler.properties.SubmissionReleaserProperties
import ebi.ac.uk.commons.http.slack.NotificationsSender
import ebi.ac.uk.commons.http.slack.Report
import mu.KotlinLogging
import uk.ac.ebi.scheduler.common.SYSTEM_NAME
import uk.ac.ebi.scheduler.common.properties.AppProperties
import uk.ac.ebi.scheduler.releaser.api.SubmissionReleaserProperties as SchedulerReleaserProps

private const val RELEASER_CORES = 4
private const val RELEASER_SUBSYSTEM = "Submission Releaser"

private val logger = KotlinLogging.logger {}

class SubmissionReleaserTrigger(
    private val appProperties: AppProperties,
    private val properties: SchedulerReleaserProps,
    private val clusterOperations: ClusterOperations,
    private val notificationsSender: NotificationsSender
) {
    fun triggerSubmissionReleaser(): Job {
        logger.info { "triggering submission releaser job" }
        return triggerJob(mode = RELEASE)
    }

    fun triggerSubmissionReleaseNotifier(): Job {
        logger.info { "triggering submission release notifier job" }
        return triggerJob(mode = NOTIFY)
    }

    private fun triggerJob(mode: ReleaserMode): Job {
        val job = submissionReleaserJob(mode)
        notificationsSender.send(Report(
            SYSTEM_NAME,
            RELEASER_SUBSYSTEM,
            "Triggered $RELEASER_SUBSYSTEM in mode $mode in the cluster job $job. Logs available at ${job.logsPath}"))

        return job
    }

    private fun submissionReleaserJob(mode: ReleaserMode): Job {
        val releaserProperties = getConfigProperties(mode, properties)
        val jobTry = clusterOperations.triggerJob(
            JobSpec(
                RELEASER_CORES,
                MemorySpec.EIGHT_GB,
                releaserProperties.asJavaCommand(appProperties.appsFolder)))

        return jobTry.fold({ throw it }, { it.apply { logger.info { "submitted job $it" } } })
    }

    private fun getConfigProperties(mode: ReleaserMode, properties: SchedulerReleaserProps) =
        SubmissionReleaserProperties.create(
            mode,
            properties.rabbitmq.host,
            properties.rabbitmq.user,
            properties.rabbitmq.password,
            properties.rabbitmq.port,
            properties.bioStudies.url,
            properties.bioStudies.user,
            properties.bioStudies.password,
            properties.notificationTimes.firstWarning,
            properties.notificationTimes.secondWarning,
            properties.notificationTimes.thirdWarning)
}
