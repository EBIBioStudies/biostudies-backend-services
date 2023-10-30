package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.persistence.common.service.StatsDataService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.submission.stats.StatsFileHandler
import ac.uk.ebi.biostd.submission.stats.SubmissionStatsService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.ac.ebi.extended.serialization.service.ExtSerializationService

@Configuration
class StatsConfig {
    @Bean
    fun statsFileHandler(): StatsFileHandler = StatsFileHandler()

    @Bean
    fun submissionStatsService(
        statsFileHandler: StatsFileHandler,
        submissionStatsService: StatsDataService,
        extSerializationService: ExtSerializationService,
        pesistenceQueryService: SubmissionPersistenceQueryService,
    ): SubmissionStatsService = SubmissionStatsService(
        statsFileHandler,
        submissionStatsService,
        extSerializationService,
        pesistenceQueryService,
    )
}
