package uk.ac.ebi.scheculer.pmc.exporter.config

import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.integration.SerializationService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.ac.ebi.scheculer.pmc.exporter.PmcExporterExecutor
import uk.ac.ebi.scheculer.pmc.exporter.service.PmcExporterService

@Configuration
class ApplicationConfig {
    @Bean
    fun exporterService(
        bioWebClient: BioWebClient,
        applicationProperties: ApplicationProperties
    ): PmcExporterService = PmcExporterService(bioWebClient, applicationProperties)

    @Bean
    fun exporterExecutor(
        pmcExporterService: PmcExporterService
    ): PmcExporterExecutor = PmcExporterExecutor(pmcExporterService)

    @Bean
    fun bioWebClient(applicationProperties: ApplicationProperties): BioWebClient =
        SecurityWebClient
            .create(applicationProperties.bioStudies.url)
            .getAuthenticatedClient(applicationProperties.bioStudies.user, applicationProperties.bioStudies.password)
}
