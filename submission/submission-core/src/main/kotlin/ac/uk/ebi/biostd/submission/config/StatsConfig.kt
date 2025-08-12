package ac.uk.ebi.biostd.submission.config

import ac.uk.ebi.biostd.persistence.common.service.StatsDataService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.filesystem.api.FileStorageService
import ac.uk.ebi.biostd.persistence.filesystem.pagetab.PageTabService
import ac.uk.ebi.biostd.submission.stats.StatsFileHandler
import ac.uk.ebi.biostd.submission.stats.SubmissionStatsCalculator
import ac.uk.ebi.biostd.submission.stats.SubmissionStatsService
import ebi.ac.uk.paths.SubmissionFolderResolver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.ac.ebi.extended.serialization.service.ExtSerializationService

@Configuration
class StatsConfig {
    @Bean
    fun statsFileHandler(): StatsFileHandler = StatsFileHandler()

    @Bean
    fun submissionStatsCalculator(
        serializationService: ExtSerializationService,
        fileStorageService: FileStorageService,
        subFolderResolver: SubmissionFolderResolver,
        pageTabService: PageTabService,
    ): SubmissionStatsCalculator = SubmissionStatsCalculator(serializationService, fileStorageService, subFolderResolver, pageTabService)

    @Bean
    fun submissionStatsService(
        statsFileHandler: StatsFileHandler,
        submissionStatsService: StatsDataService,
        submissionStatsCalculator: SubmissionStatsCalculator,
        persistenceQueryService: SubmissionPersistenceQueryService,
    ): SubmissionStatsService =
        SubmissionStatsService(
            statsFileHandler,
            submissionStatsService,
            persistenceQueryService,
            submissionStatsCalculator,
        )
}
