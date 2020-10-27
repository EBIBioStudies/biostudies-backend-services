package uk.ac.ebi.scheduler.common.config

import ac.uk.ebi.cluster.client.lsf.ClusterOperations
import uk.ac.ebi.scheduler.common.properties.AppProperties
import uk.ac.ebi.scheduler.common.properties.SshProperties
import uk.ac.ebi.scheduler.pmc.importer.api.PmcProcessorProp
import uk.ac.ebi.scheduler.pmc.importer.domain.PmcLoaderService
import uk.ac.ebi.scheduler.scheduling.DailyScheduler
import ebi.ac.uk.commons.http.slack.NotificationsSender
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import uk.ac.ebi.scheduler.releaser.api.SubmissionReleaserProperties
import uk.ac.ebi.scheduler.releaser.domain.SubmissionReleaserService

@Configuration
@EnableScheduling
internal class SchedulerConfig {
    @Bean
    fun clusterOperations(sshProperties: SshProperties) = ClusterOperations.create(
        sshProperties.user,
        sshProperties.password,
        sshProperties.server)

    @Bean
    fun loaderService(
        clusterOperations: ClusterOperations,
        properties: PmcProcessorProp,
        appProperties: AppProperties,
        notificationsSender: NotificationsSender
    ): PmcLoaderService = PmcLoaderService(clusterOperations, properties, appProperties, notificationsSender)

    @Bean
    fun submissionReleaserService(
        appProperties: AppProperties,
        clusterOperations: ClusterOperations,
        releaserProperties: SubmissionReleaserProperties
    ): SubmissionReleaserService = SubmissionReleaserService(appProperties, releaserProperties, clusterOperations)

    @Bean
    fun scheduler(
        loaderService: PmcLoaderService,
        releaserService: SubmissionReleaserService
    ): DailyScheduler = DailyScheduler(loaderService, releaserService)
}
