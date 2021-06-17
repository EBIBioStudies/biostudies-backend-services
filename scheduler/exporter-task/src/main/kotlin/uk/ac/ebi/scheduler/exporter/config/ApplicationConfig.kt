package uk.ac.ebi.scheduler.exporter.config

import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.integration.SerializationConfig
import ac.uk.ebi.biostd.integration.SerializationService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.ac.ebi.scheduler.exporter.ExporterExecutor
import uk.ac.ebi.scheduler.exporter.service.ExporterService

@Configuration
class ApplicationConfig {
    @Bean
    fun exporterService(
        bioWebClient: BioWebClient,
        serializationService: SerializationService,
        applicationProperties: ApplicationProperties
    ): ExporterService = ExporterService(bioWebClient, applicationProperties, serializationService)

    @Bean
    fun exporterExecutor(exporterService: ExporterService): ExporterExecutor = ExporterExecutor(exporterService)

    @Bean
    fun bioWebClient(applicationProperties: ApplicationProperties): BioWebClient =
        SecurityWebClient
            .create(applicationProperties.bioStudies.url)
            .getAuthenticatedClient(applicationProperties.bioStudies.user, applicationProperties.bioStudies.password)

    @Bean
    fun serializationService(): SerializationService = SerializationConfig.serializationService()
}
