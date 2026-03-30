package ac.uk.ebi.biostd.submission.config

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.persistence.common.service.StatsDataService
import ac.uk.ebi.biostd.submission.stats.common.StatsFileHandler
import ac.uk.ebi.biostd.submission.stats.service.StatsReporterService
import ac.uk.ebi.biostd.submission.stats.service.SubmissionStatsService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class StatsConfig {
    @Bean
    fun statsFileHandler(): StatsFileHandler = StatsFileHandler()

    @Bean
    fun submissionStatsService(
        statsFileHandler: StatsFileHandler,
        submissionStatsService: StatsDataService,
    ): SubmissionStatsService = SubmissionStatsService(statsFileHandler, submissionStatsService)

    @Bean
    fun statsReporterService(
        statsDataService: StatsDataService,
        applicationProperties: ApplicationProperties,
    ): StatsReporterService = StatsReporterService(statsDataService, applicationProperties.persistence)
}
