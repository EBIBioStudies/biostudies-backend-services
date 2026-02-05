package ac.uk.ebi.biostd.migration

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionStatsDataRepository
import ac.uk.ebi.biostd.submission.domain.extended.ExtSubmissionService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MigrationConfig {
    @Bean
    fun migrationService(
        applicationProperties: ApplicationProperties,
        statsRepository: SubmissionStatsDataRepository,
        submissionRequestRepository: SubmissionRequestDocDataRepository,
        extSubmissionService: ExtSubmissionService,
    ): MigrationService =
        MigrationService(
            applicationProperties.migration,
            statsRepository,
            submissionRequestRepository,
            extSubmissionService,
        )
}
