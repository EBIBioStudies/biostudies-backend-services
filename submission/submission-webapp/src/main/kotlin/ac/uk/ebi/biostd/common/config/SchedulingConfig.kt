package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.admin.operations.OperationsScheduler
import ac.uk.ebi.biostd.admin.operations.OperationsService
import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.stats.web.TempFileGenerator
import ac.uk.ebi.biostd.submission.stats.service.StatsReporterService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableScheduling
@Profile("!openapi-gen")
class SchedulingConfig {
    @Bean
    fun operationsService(
        applicationProperties: ApplicationProperties,
        persistenceService: SubmissionRequestPersistenceService,
        tempFileGenerator: TempFileGenerator,
    ): OperationsService = OperationsService(applicationProperties, persistenceService, tempFileGenerator)

    @Bean
    fun operationsScheduler(
        applicationProperties: ApplicationProperties,
        statsReporterService: StatsReporterService,
        operationsService: OperationsService,
    ): OperationsScheduler = OperationsScheduler(applicationProperties, operationsService, statsReporterService)
}
