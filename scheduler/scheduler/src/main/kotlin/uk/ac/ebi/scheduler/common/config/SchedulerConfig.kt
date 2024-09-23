package uk.ac.ebi.scheduler.common.config

import ebi.ac.uk.commons.http.slack.NotificationsSender
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import uk.ac.ebi.biostd.client.cluster.api.ClusterClient
import uk.ac.ebi.biostd.client.cluster.api.LsfClusterClient
import uk.ac.ebi.biostd.client.cluster.api.SlurmClusterClient
import uk.ac.ebi.biostd.client.cluster.model.Cluster.LSF
import uk.ac.ebi.biostd.client.cluster.model.Cluster.SLURM
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
    fun clusterOperations(appProperties: AppProperties): ClusterClient {
        return when (appProperties.cluster.default) {
            LSF -> lsfCluster(appProperties)
            SLURM -> slurmCluster(appProperties)
        }
    }

    @Bean
    fun loaderService(
        clusterClient: ClusterClient,
        properties: PmcProcessorProp,
        appProperties: AppProperties,
        @Qualifier("pmcNotificationsSender") pmcNotificationsSender: NotificationsSender,
    ): PmcLoaderService = PmcLoaderService(clusterClient, properties, appProperties, pmcNotificationsSender)

    @Bean
    fun submissionReleaserTrigger(
        appProperties: AppProperties,
        clusterClient: ClusterClient,
        releaserProperties: SubmissionReleaserProperties,
        @Qualifier("schedulerNotificationsSender") schedulerNotificationsSender: NotificationsSender,
    ): SubmissionReleaserTrigger = SubmissionReleaserTrigger(appProperties, releaserProperties, clusterClient, schedulerNotificationsSender)

    @Bean
    fun exporterTrigger(
        appProperties: AppProperties,
        clusterClient: ClusterClient,
        exporterProperties: ExporterProperties,
        @Qualifier("pmcNotificationsSender") pmcNotificationsSender: NotificationsSender,
        @Qualifier("schedulerNotificationsSender") schedulerNotificationsSender: NotificationsSender,
    ): ExporterTrigger =
        ExporterTrigger(
            appProperties,
            exporterProperties,
            clusterClient,
            pmcNotificationsSender,
            schedulerNotificationsSender,
        )

    @Bean
    fun statsReporterTrigger(
        appProperties: AppProperties,
        properties: StatsReporterProperties,
        clusterClient: ClusterClient,
        schedulerNotificationsSender: NotificationsSender,
    ): StatsReporterTrigger =
        StatsReporterTrigger(
            appProperties,
            properties,
            clusterClient,
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

    companion object {
        fun lsfCluster(properties: AppProperties): LsfClusterClient {
            return LsfClusterClient.create(
                properties.cluster.sshKey,
                properties.cluster.lsfServer,
                properties.cluster.logsPath,
            )
        }

        fun slurmCluster(properties: AppProperties): SlurmClusterClient {
            return SlurmClusterClient.create(
                properties.cluster.sshKey,
                properties.cluster.slurmServer,
                properties.cluster.logsPath,
                properties.cluster.wrapperPath,
            )
        }
    }
}
