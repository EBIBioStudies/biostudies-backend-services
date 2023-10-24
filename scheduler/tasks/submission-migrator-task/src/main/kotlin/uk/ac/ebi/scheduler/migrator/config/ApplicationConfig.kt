package uk.ac.ebi.scheduler.migrator.config

import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.persistence.doc.MongoDbReactiveConfig
import ac.uk.ebi.biostd.persistence.doc.db.reactive.repositories.SubmissionMigratorRepository
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import uk.ac.ebi.scheduler.migrator.SubmissionMigratorExecutor
import uk.ac.ebi.scheduler.migrator.service.SubmissionMigratorService

@Configuration
@Import(MongoDbReactiveConfig::class)
@EnableConfigurationProperties(ApplicationProperties::class)
class ApplicationConfig(
    private val migratorRepository: SubmissionMigratorRepository,
    private val properties: ApplicationProperties,
) {
    @Bean
    fun submissionMigratorService(
        bioWebClient: BioWebClient,
    ): SubmissionMigratorService = SubmissionMigratorService(properties, bioWebClient, migratorRepository)

    @Bean
    fun submissionMigratorExecutor(
        submissionMigratorService: SubmissionMigratorService
    ): SubmissionMigratorExecutor = SubmissionMigratorExecutor(submissionMigratorService)

    @Bean
    fun bioWebClient(): BioWebClient =
        SecurityWebClient
            .create(properties.bioStudies.url)
            .getAuthenticatedClient(properties.bioStudies.user, properties.bioStudies.password)
}
