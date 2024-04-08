package uk.ac.ebi.scheduler.stats.config

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
    fun statsReporterService(statsRepository: StatsReporterDataRepository): StatsReporterService =
        StatsReporterService(appProperties, statsRepository)

    @Bean
    fun statsReporterExecutor(service: StatsReporterService): StatsReporterExecutor = StatsReporterExecutor(service)
}
