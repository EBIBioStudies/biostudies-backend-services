package uk.ac.ebi.scheduler.exporter.config

import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.integration.SerializationConfig
import ac.uk.ebi.biostd.integration.SerializationService
import com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import ebi.ac.uk.extended.mapping.to.ToFileListMapper
import ebi.ac.uk.extended.mapping.to.ToSectionMapper
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
import org.apache.commons.net.PrintCommandListener
import org.apache.commons.net.ftp.FTPClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.ac.ebi.extended.serialization.integration.ExtSerializationConfig
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.scheduler.exporter.ExporterExecutor
import uk.ac.ebi.scheduler.exporter.persistence.PmcRepository
import uk.ac.ebi.scheduler.exporter.service.ExporterService
import uk.ac.ebi.scheduler.exporter.service.PmcExporterService
import uk.ac.ebi.scheduler.exporter.service.PublicOnlyExporterService
import java.io.PrintWriter

internal const val BUFFER_SIZE = 1024 * 1024

@Configuration
@Suppress("TooManyFunctions")
class ApplicationConfig(
    private val pmcRepository: PmcRepository,
) {
    @Bean
    fun pmcExporterService(
        xmlWriter: XmlMapper,
        ftpClient: FTPClient,
        applicationProperties: ApplicationProperties,
    ): PmcExporterService = PmcExporterService(pmcRepository, xmlWriter, ftpClient, applicationProperties)

    @Bean
    fun publicOnlyExporterService(
        bioWebClient: BioWebClient,
        serializationService: SerializationService,
        applicationProperties: ApplicationProperties,
        toSubmissionMapper: ToSubmissionMapper,
    ): PublicOnlyExporterService =
        PublicOnlyExporterService(bioWebClient, applicationProperties, serializationService, toSubmissionMapper)

    @Bean
    fun toSubmissionMapper(toSectionMapper: ToSectionMapper): ToSubmissionMapper = ToSubmissionMapper(toSectionMapper)

    @Bean
    fun toSectionMapper(toFileListMapper: ToFileListMapper) = ToSectionMapper(toFileListMapper)

    @Bean
    fun toFileListMapper(serializationService: ExtSerializationService) = ToFileListMapper(serializationService)

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
    fun ftpClient(): FTPClient = FTPClient().apply {
        bufferSize = BUFFER_SIZE
        addProtocolCommandListener(PrintCommandListener(PrintWriter(System.out)))
    }

    @Bean
    fun xmlWriter(): XmlMapper = XmlMapper(
        JacksonXmlModule().apply { setDefaultUseWrapper(false) }
    ).apply { enable(INDENT_OUTPUT) }

    @Bean
    fun serializationService(): SerializationService = SerializationConfig.serializationService()

    @Bean
    fun extSerializationService(): ExtSerializationService = ExtSerializationConfig.extSerializationService()
}
