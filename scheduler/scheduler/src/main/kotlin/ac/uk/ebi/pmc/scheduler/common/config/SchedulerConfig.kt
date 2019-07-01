package ac.uk.ebi.pmc.scheduler.common.config

import ac.uk.ebi.cluster.client.lsf.ClusterOperations
import ac.uk.ebi.pmc.scheduler.common.properties.AppProperties
import ac.uk.ebi.pmc.scheduler.common.properties.SshProperties
import ac.uk.ebi.pmc.scheduler.pmc.importer.api.PmcProcessorProp
import ac.uk.ebi.pmc.scheduler.pmc.importer.domain.PmcLoaderService
import ac.uk.ebi.pmc.scheduler.pmc.importer.scheduling.DailyScheduler
import ebi.ac.uk.commons.http.slack.NotificationsSender
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

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
    ) =
        PmcLoaderService(clusterOperations, properties, appProperties, notificationsSender)

    @Bean
    fun scheduler(loaderService: PmcLoaderService) = DailyScheduler(loaderService)
}
