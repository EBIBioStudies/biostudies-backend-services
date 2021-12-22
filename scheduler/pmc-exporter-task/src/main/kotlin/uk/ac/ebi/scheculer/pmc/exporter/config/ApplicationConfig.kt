package uk.ac.ebi.scheculer.pmc.exporter.config

import ac.uk.ebi.biostd.integration.SerializationConfig
import ac.uk.ebi.biostd.integration.SerializationService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.ac.ebi.extended.serialization.integration.ExtSerializationConfig
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.scheculer.pmc.exporter.PmcExporterExecutor
import uk.ac.ebi.scheculer.pmc.exporter.persistence.PmcRepository
import uk.ac.ebi.scheculer.pmc.exporter.service.PmcExporterService

@Configuration
class ApplicationConfig(
    private val pmcRepository: PmcRepository
) {
    @Bean
    fun exporterService(
        applicationProperties: ApplicationProperties,
    ): PmcExporterService = PmcExporterService(pmcRepository, applicationProperties)

    @Bean
    fun exporterExecutor(
        pmcExporterService: PmcExporterService
    ): PmcExporterExecutor = PmcExporterExecutor(pmcExporterService)

    @Bean
    fun extSerializationService(): ExtSerializationService = ExtSerializationConfig.extSerializationService()

    @Bean
    fun serializationService(): SerializationService = SerializationConfig.serializationService()
}
