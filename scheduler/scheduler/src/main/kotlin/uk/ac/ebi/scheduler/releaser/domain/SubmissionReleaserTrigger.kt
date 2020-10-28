package uk.ac.ebi.scheduler.releaser.domain

import ac.uk.ebi.cluster.client.lsf.ClusterOperations
import ac.uk.ebi.cluster.client.model.Job
import ac.uk.ebi.cluster.client.model.JobSpec
import ac.uk.ebi.cluster.client.model.MemorySpec
import ac.uk.ebi.scheduler.properties.SubmissionReleaserProperties
import mu.KotlinLogging
import uk.ac.ebi.scheduler.common.properties.AppProperties
import uk.ac.ebi.scheduler.releaser.api.SubmissionReleaserProperties as SchedulerReleaserProps

private const val RELEASER_CORES = 4

private val logger = KotlinLogging.logger {}

class SubmissionReleaserTrigger(
    private val appProperties: AppProperties,
    private val properties: SchedulerReleaserProps,
    private val clusterOperations: ClusterOperations
) {
    fun triggerSubmissionReleaser(): Job {
        logger.info { "triggering submission releaser job" }
        val releaserProperties = getConfigProperties(properties)
        val jobTry = clusterOperations.triggerJob(
            JobSpec(
                RELEASER_CORES,
                MemorySpec.EIGHT_GB,
                releaserProperties.asJavaCommand(appProperties.appsFolder)))

        return jobTry.fold({ throw it }, { it.apply { logger.info { "submitted job $it" } } })
    }

    private fun getConfigProperties(properties: SchedulerReleaserProps) =
        SubmissionReleaserProperties.create(
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
