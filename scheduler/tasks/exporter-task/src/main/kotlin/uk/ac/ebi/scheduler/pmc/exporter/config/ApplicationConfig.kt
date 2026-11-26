package uk.ac.ebi.scheduler.pmc.exporter.config

import com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import uk.ac.ebi.biostd.client.integration.web.BioWebClient
import uk.ac.ebi.biostd.client.integration.web.SecurityWebClient
import uk.ac.ebi.biostd.common.SerializationConfig
import uk.ac.ebi.biostd.coroutines.RetryConfig
import uk.ac.ebi.biostd.coroutines.SuspendRetryTemplate
import uk.ac.ebi.biostd.extended.mapping.to.ToFileListMapper
import uk.ac.ebi.biostd.extended.mapping.to.ToLinkListMapper
import uk.ac.ebi.biostd.extended.mapping.to.ToSectionMapper
import uk.ac.ebi.biostd.extended.mapping.to.ToSubmissionMapper
import uk.ac.ebi.biostd.extended.serialization.integration.ExtSerializationConfig
import uk.ac.ebi.biostd.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.biostd.integration.SerializationService
import uk.ac.ebi.biostd.serialization.common.FilesResolver
import uk.ac.ebi.scheduler.pmc.exporter.ExporterExecutor
import uk.ac.ebi.scheduler.pmc.exporter.persistence.PmcDataRepository
import uk.ac.ebi.scheduler.pmc.exporter.persistence.PmcRepository
import uk.ac.ebi.scheduler.pmc.exporter.service.ExporterService
import uk.ac.ebi.scheduler.pmc.exporter.service.PmcExporterService
import uk.ac.ebi.scheduler.pmc.exporter.service.PublicOnlyExporterService
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
    fun toSectionMapper(
        toFileListMapper: ToFileListMapper,
        toLinkListMapper: ToLinkListMapper,
    ) = ToSectionMapper(toFileListMapper, toLinkListMapper)

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
    fun toLinkListMapper(
        serializationService: SerializationService,
        extSerializationService: ExtSerializationService,
        resolver: FilesResolver,
    ) = ToLinkListMapper(serializationService, extSerializationService, resolver)

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
