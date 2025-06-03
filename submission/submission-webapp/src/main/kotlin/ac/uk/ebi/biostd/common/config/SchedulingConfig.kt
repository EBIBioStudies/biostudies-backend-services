package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.admin.operations.OperationsScheduler
import ac.uk.ebi.biostd.admin.operations.OperationsService
import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.stats.web.TempFileGenerator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableScheduling
class SchedulingConfig {
    @Bean
    fun operationsService(
        applicationProperties: ApplicationProperties,
        persistenceService: SubmissionRequestPersistenceService,
        tempFileGenerator: TempFileGenerator,
    ): OperationsService = OperationsService(applicationProperties, persistenceService, tempFileGenerator)

    @Bean
    fun operationsScheduler(operationsService: OperationsService): OperationsScheduler = OperationsScheduler(operationsService)
}
