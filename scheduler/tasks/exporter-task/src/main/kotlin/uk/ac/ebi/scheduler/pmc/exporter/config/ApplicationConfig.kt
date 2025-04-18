package uk.ac.ebi.scheduler.pmc.exporter.config

import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.common.SerializationConfig
import ac.uk.ebi.biostd.integration.SerializationService
import com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import ebi.ac.uk.coroutines.RetryConfig
import ebi.ac.uk.coroutines.SuspendRetryTemplate
import ebi.ac.uk.extended.mapping.to.ToFileListMapper
import ebi.ac.uk.extended.mapping.to.ToSectionMapper
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import uk.ac.ebi.extended.serialization.integration.ExtSerializationConfig
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.scheduler.pmc.exporter.ExporterExecutor
import uk.ac.ebi.scheduler.pmc.exporter.persistence.PmcDataRepository
import uk.ac.ebi.scheduler.pmc.exporter.persistence.PmcRepository
import uk.ac.ebi.scheduler.pmc.exporter.service.ExporterService
import uk.ac.ebi.scheduler.pmc.exporter.service.PmcExporterService
import uk.ac.ebi.scheduler.pmc.exporter.service.PublicOnlyExporterService
import uk.ac.ebi.serialization.common.FilesResolver
import java.io.File

internal const val BUFFER_SIZE = 1024 * 1024

@Configuration
@Suppress("TooManyFunctions")
class ApplicationConfig {
    @Bean
    fun pmcDataReposotory(
        pmcRepository: PmcRepository,
        mongoTemplate: ReactiveMongoTemplate,
    ): PmcDataRepository = PmcDataRepository(pmcRepository, mongoTemplate)

    @Bean
    fun suspendRetryTemplate(): SuspendRetryTemplate =
        SuspendRetryTemplate(
            RetryConfig(
                maxAttempts = 10,
                initialInterval = 300L,
                multiplier = 1.2,
                maxInterval = 10_000L,
            ),
        )

    @Bean
    fun pmcExporterService(
        xmlWriter: XmlMapper,
        applicationProperties: ApplicationProperties,
        retryTemplate: SuspendRetryTemplate,
        pmcDataRepository: PmcDataRepository,
    ): PmcExporterService = PmcExporterService(pmcDataRepository, xmlWriter, applicationProperties, retryTemplate)

    @Bean
    fun publicOnlyExporterService(
        bioWebClient: BioWebClient,
        serializationService: SerializationService,
        applicationProperties: ApplicationProperties,
        toSubmissionMapper: ToSubmissionMapper,
    ): PublicOnlyExporterService = PublicOnlyExporterService(bioWebClient, applicationProperties, serializationService, toSubmissionMapper)

    @Bean
    fun toSubmissionMapper(toSectionMapper: ToSectionMapper): ToSubmissionMapper = ToSubmissionMapper(toSectionMapper)

    @Bean
    fun toSectionMapper(toFileListMapper: ToFileListMapper) = ToSectionMapper(toFileListMapper)

    @Bean
    fun folderResolver(applicationProperties: ApplicationProperties): FilesResolver =
        FilesResolver(File(applicationProperties.tmpFilesPath))

    @Bean
    fun toFileListMapper(
        serializationService: SerializationService,
        extSerializationService: ExtSerializationService,
        resolver: FilesResolver,
    ) = ToFileListMapper(serializationService, extSerializationService, resolver)

    @Bean
    fun exporterService(
        pmcExporterService: PmcExporterService,
        publicOnlyExporterService: PublicOnlyExporterService,
    ): ExporterService = ExporterService(pmcExporterService, publicOnlyExporterService)

    @Bean
    fun exporterExecutor(
        exporterService: ExporterService,
        applicationProperties: ApplicationProperties,
    ): ExporterExecutor = ExporterExecutor(exporterService, applicationProperties)

    @Bean
    fun bioWebClient(applicationProperties: ApplicationProperties): BioWebClient =
        SecurityWebClient
            .create(applicationProperties.bioStudies.url)
            .getAuthenticatedClient(applicationProperties.bioStudies.user, applicationProperties.bioStudies.password)

    @Bean
    fun xmlWriter(): XmlMapper =
        XmlMapper(
            JacksonXmlModule().apply { setDefaultUseWrapper(false) },
        ).apply { enable(INDENT_OUTPUT) }

    @Bean
    fun serializationService(): SerializationService = SerializationConfig.serializationService()

    @Bean
    fun extSerializationService(): ExtSerializationService = ExtSerializationConfig.extSerializationService()
}
