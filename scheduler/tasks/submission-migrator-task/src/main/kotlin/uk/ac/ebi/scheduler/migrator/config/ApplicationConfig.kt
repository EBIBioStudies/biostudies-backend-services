package uk.ac.ebi.scheduler.migrator.config

import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.ac.ebi.scheduler.migrator.SubmissionMigratorExecutor
import uk.ac.ebi.scheduler.migrator.persistence.MigratorRepository
import uk.ac.ebi.scheduler.migrator.service.SubmissionMigratorService

@Configuration
@EnableConfigurationProperties(ApplicationProperties::class)
class ApplicationConfig(
    private val migratorRepository: MigratorRepository,
    private val appProperties: ApplicationProperties,
) {
    @Bean
    fun submissionMigratorService(
        bioWebClient: BioWebClient,
    ): SubmissionMigratorService = SubmissionMigratorService(bioWebClient, migratorRepository)

    @Bean
    fun submissionMigratorExecutor(
        submissionMigratorService: SubmissionMigratorService
    ): SubmissionMigratorExecutor = SubmissionMigratorExecutor(submissionMigratorService)

    @Bean
    fun bioWebClient(): BioWebClient =
        SecurityWebClient
            .create(appProperties.bioStudies.url)
            .getAuthenticatedClient(appProperties.bioStudies.user, appProperties.bioStudies.password)
}
