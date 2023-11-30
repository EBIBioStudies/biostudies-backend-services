package uk.ac.ebi.scheduler.common.config

import ebi.ac.uk.commons.http.slack.NotificationsSender
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import uk.ac.ebi.biostd.client.cluster.api.ClusterClient
import uk.ac.ebi.biostd.client.cluster.api.ClusterOperations
import uk.ac.ebi.scheduler.common.properties.AppProperties
import uk.ac.ebi.scheduler.pmc.exporter.api.ExporterProperties
import uk.ac.ebi.scheduler.pmc.exporter.domain.ExporterTrigger
import uk.ac.ebi.scheduler.pmc.importer.api.PmcProcessorProp
import uk.ac.ebi.scheduler.pmc.importer.domain.PmcLoaderService
import uk.ac.ebi.scheduler.releaser.api.SubmissionReleaserProperties
import uk.ac.ebi.scheduler.releaser.domain.SubmissionReleaserTrigger
import uk.ac.ebi.scheduler.scheduling.DailyScheduler
import uk.ac.ebi.scheduler.stats.api.StatsReporterProperties
import uk.ac.ebi.scheduler.stats.domain.StatsReporterTrigger

@Configuration
@EnableScheduling
@EnableConfigurationProperties(AppProperties::class, StatsReporterProperties::class)
internal class SchedulerConfig {
    @Bean
    fun clusterOperations(
        appProperties: AppProperties,
    ): ClusterOperations = ClusterClient.create(
        appProperties.cluster.sshKey,
        appProperties.cluster.server,
        appProperties.cluster.logsPath,
    )

    @Bean
    fun loaderService(
        clusterOperations: ClusterOperations,
        properties: PmcProcessorProp,
        appProperties: AppProperties,
        @Qualifier("pmcNotificationsSender") pmcNotificationsSender: NotificationsSender,
    ): PmcLoaderService = PmcLoaderService(clusterOperations, properties, appProperties, pmcNotificationsSender)

    @Bean
    fun submissionReleaserTrigger(
        appProperties: AppProperties,
        clusterOperations: ClusterOperations,
        releaserProperties: SubmissionReleaserProperties,
        @Qualifier("schedulerNotificationsSender") schedulerNotificationsSender: NotificationsSender,
    ): SubmissionReleaserTrigger =
        SubmissionReleaserTrigger(appProperties, releaserProperties, clusterOperations, schedulerNotificationsSender)

    @Bean
    fun exporterTrigger(
        appProperties: AppProperties,
        clusterOperations: ClusterOperations,
        exporterProperties: ExporterProperties,
        @Qualifier("pmcNotificationsSender") pmcNotificationsSender: NotificationsSender,
        @Qualifier("schedulerNotificationsSender") schedulerNotificationsSender: NotificationsSender,
    ): ExporterTrigger = ExporterTrigger(
        appProperties,
        exporterProperties,
        clusterOperations,
        pmcNotificationsSender,
        schedulerNotificationsSender,
    )

    @Bean
    fun statsReporterTrigger(
        appProperties: AppProperties,
        properties: StatsReporterProperties,
        clusterOperations: ClusterOperations,
        schedulerNotificationsSender: NotificationsSender,
    ): StatsReporterTrigger = StatsReporterTrigger(
        appProperties,
        properties,
        clusterOperations,
        schedulerNotificationsSender,
    )

    @Bean
    fun scheduler(
        appProperties: AppProperties,
        loaderService: PmcLoaderService,
        exporterTrigger: ExporterTrigger,
        statsTrigger: StatsReporterTrigger,
        releaserTrigger: SubmissionReleaserTrigger,
    ): DailyScheduler =
        DailyScheduler(
            appProperties.dailyScheduling,
            exporterTrigger,
            loaderService,
            statsTrigger,
            releaserTrigger,
        )
}
