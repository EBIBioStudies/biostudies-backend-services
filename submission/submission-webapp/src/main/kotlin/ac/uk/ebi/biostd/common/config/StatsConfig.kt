package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.repositories.SubmissionStatsDataRepository
import ac.uk.ebi.biostd.stats.web.handlers.StatsFileHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.ac.ebi.stats.service.SubmissionStatsService

@Configuration
class StatsConfig(
    private val submissionQueryService: SubmissionQueryService,
    private val submissionStatsRepository: SubmissionStatsDataRepository
) {
    @Bean
    fun statsFileHandler(): StatsFileHandler = StatsFileHandler()

    @Bean
    fun submissionStatsService(): SubmissionStatsService =
        SubmissionStatsService(submissionQueryService, submissionStatsRepository)
}
