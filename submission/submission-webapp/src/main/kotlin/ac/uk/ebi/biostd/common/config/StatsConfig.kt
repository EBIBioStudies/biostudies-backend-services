package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.stats.web.handlers.StatsFileHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.ac.ebi.stats.persistence.repositories.SubmissionStatsRepository
import uk.ac.ebi.stats.service.SubmissionStatsService

@Configuration
class StatsConfig(private val submissionStatsRepository: SubmissionStatsRepository) {
    @Bean
    fun statsFileHandler(): StatsFileHandler = StatsFileHandler()

    @Bean
    fun submissionStatsService(): SubmissionStatsService = SubmissionStatsService(submissionStatsRepository)
}
