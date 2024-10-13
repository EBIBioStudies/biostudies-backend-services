package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.archive.ArchiveScheduler
import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableScheduling
class SchedulingConfig {
    @Bean
    fun archiveScheduler(
        applicationProperties: ApplicationProperties,
        persistenceService: SubmissionRequestPersistenceService,
    ): ArchiveScheduler = ArchiveScheduler(applicationProperties, persistenceService)
}
