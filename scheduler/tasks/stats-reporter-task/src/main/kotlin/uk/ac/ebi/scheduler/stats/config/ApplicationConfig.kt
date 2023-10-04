package uk.ac.ebi.scheduler.stats.config

import ac.uk.ebi.cluster.client.lsf.ClusterOperations
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import uk.ac.ebi.scheduler.stats.StatsReporterExecutor
import uk.ac.ebi.scheduler.stats.persistence.StatsReporterDataRepository
import uk.ac.ebi.scheduler.stats.service.StatsReporterService

@Configuration
@Import(PersistenceConfig::class)
@EnableConfigurationProperties(ApplicationProperties::class)
class ApplicationConfig(
    private val appProperties: ApplicationProperties,
) {
    @Bean
    fun clusterOperations(): ClusterOperations =
        ClusterOperations.create(appProperties.ssh.key, appProperties.ssh.server)

    @Bean
    fun statsReporterService(
        clusterOperations: ClusterOperations,
        statsRepository: StatsReporterDataRepository
    ): StatsReporterService = StatsReporterService(appProperties, clusterOperations, statsRepository)

    @Bean
    fun statsReporterExecutor(service: StatsReporterService): StatsReporterExecutor = StatsReporterExecutor(service)
}
