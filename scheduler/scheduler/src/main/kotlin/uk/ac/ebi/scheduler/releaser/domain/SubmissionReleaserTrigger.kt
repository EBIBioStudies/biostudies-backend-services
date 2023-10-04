package uk.ac.ebi.scheduler.releaser.domain

import ac.uk.ebi.cluster.client.lsf.ClusterOperations
import ac.uk.ebi.cluster.client.model.CoresSpec.FOUR_CORES
import ac.uk.ebi.cluster.client.model.Job
import ac.uk.ebi.cluster.client.model.JobSpec
import ac.uk.ebi.cluster.client.model.MemorySpec.Companion.EIGHT_GB
import ac.uk.ebi.cluster.client.model.logsPath
import ac.uk.ebi.scheduler.properties.ReleaserMode
import ac.uk.ebi.scheduler.properties.ReleaserMode.GENERATE_FTP_LINKS
import ac.uk.ebi.scheduler.properties.ReleaserMode.NOTIFY
import ac.uk.ebi.scheduler.properties.ReleaserMode.RELEASE
import ac.uk.ebi.scheduler.properties.SubmissionReleaserProperties
import ebi.ac.uk.commons.http.slack.NotificationsSender
import ebi.ac.uk.commons.http.slack.Report
import mu.KotlinLogging
import uk.ac.ebi.scheduler.common.SYSTEM_NAME
import uk.ac.ebi.scheduler.common.properties.AppProperties
import uk.ac.ebi.scheduler.releaser.api.SubmissionReleaserProperties as SchedulerReleaserProps

internal const val RELEASER_SUBSYSTEM = "Submission Releaser"

private val logger = KotlinLogging.logger {}

internal class SubmissionReleaserTrigger(
    private val appProperties: AppProperties,
    private val properties: SchedulerReleaserProps,
    private val clusterOperations: ClusterOperations,
    private val schedulerNotificationsSender: NotificationsSender,
) {
    fun triggerSubmissionReleaser(debugPort: Int? = null): Job {
        logger.info { "triggering submission releaser job" }
        return triggerJob(mode = RELEASE, debugPort)
    }

    fun triggerSubmissionReleaseNotifier(debugPort: Int? = null): Job {
        logger.info { "triggering submission release notifier job" }
        return triggerJob(mode = NOTIFY, debugPort)
    }

    fun triggerFtpLinksGenerator(debugPort: Int? = null): Job {
        logger.info { "triggering ftp links generator job" }
        return triggerJob(mode = GENERATE_FTP_LINKS, debugPort)
    }

    private fun triggerJob(mode: ReleaserMode, debugPort: Int?): Job {
        val job = submissionReleaserJob(mode, debugPort)
        schedulerNotificationsSender.send(
            Report(
                SYSTEM_NAME,
                RELEASER_SUBSYSTEM,
                "Triggered $RELEASER_SUBSYSTEM in mode $mode in the cluster job $job. Logs available at ${job.logsPath}"
            )
        )

        return job
    }

    private fun submissionReleaserJob(mode: ReleaserMode, debugPort: Int?): Job {
        val releaserProperties = getConfigProperties(mode, properties)
        val jobTry = clusterOperations.triggerJob(
            JobSpec(
                cores = FOUR_CORES,
                ram = EIGHT_GB,
                command = releaserProperties.asCmd(appProperties.appsFolder, debugPort)
            )
        )

        return jobTry.fold({ throw it }, { it.apply { logger.info { "submitted job $it" } } })
    }

    private fun getConfigProperties(mode: ReleaserMode, properties: SchedulerReleaserProps) =
        SubmissionReleaserProperties.create(
            mode,
            properties.persistence.database,
            properties.persistence.uri,
            properties.rabbitmq.host,
            properties.rabbitmq.user,
            properties.rabbitmq.password,
            properties.rabbitmq.port,
            properties.bioStudies.url,
            properties.bioStudies.user,
            properties.bioStudies.password,
            properties.notificationTimes.firstWarningDays,
            properties.notificationTimes.secondWarningDays,
            properties.notificationTimes.thirdWarningDays
        )
}
