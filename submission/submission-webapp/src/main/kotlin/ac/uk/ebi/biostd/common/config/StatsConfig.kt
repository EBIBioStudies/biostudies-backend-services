package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.persistence.common.service.StatsDataService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionMetaQueryService
import ac.uk.ebi.biostd.persistence.repositories.SubmissionStatsDataRepository
import ac.uk.ebi.biostd.persistence.service.StatsSqlDataService
import ac.uk.ebi.biostd.stats.web.handlers.StatsFileHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class StatsConfig(
    private val submissionQueryService: SubmissionMetaQueryService,
    private val submissionStatsRepository: SubmissionStatsDataRepository
) {
    @Bean
    fun statsFileHandler(): StatsFileHandler = StatsFileHandler()

    @Bean
    fun submissionStatsService(): StatsDataService =
        StatsSqlDataService(submissionQueryService, submissionStatsRepository)
}
