package ac.uk.ebi.pmc.scheduler.common.config

import ac.uk.ebi.cluster.client.lsf.ClusterOperations
import ac.uk.ebi.pmc.scheduler.common.properties.AppProperties
import ac.uk.ebi.pmc.scheduler.common.properties.SshProperties
import ac.uk.ebi.pmc.scheduler.pmc.importer.PmcImporterService
import ac.uk.ebi.pmc.scheduler.pmc.importer.SchedulerPmcImporterProp
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SchedulerConfig {

    @Bean
    fun clusterOperations(sshProperties: SshProperties) = ClusterOperations.create(
        sshProperties.user,
        sshProperties.password,
        sshProperties.server)

    @Bean
    fun pmcImporter(
        clusterOperations: ClusterOperations,
        properties: SchedulerPmcImporterProp,
        appProperties: AppProperties
    ) =
        PmcImporterService(clusterOperations, properties, appProperties)
}
