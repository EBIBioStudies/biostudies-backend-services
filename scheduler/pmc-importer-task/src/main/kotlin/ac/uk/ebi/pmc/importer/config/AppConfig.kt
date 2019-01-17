package ac.uk.ebi.pmc.importer.config

import ac.uk.ebi.biostd.SerializationService
import ac.uk.ebi.pmc.importer.client.PmcApi
import ac.uk.ebi.pmc.importer.data.MongoDocService
import ac.uk.ebi.pmc.importer.data.MongoRepository
import ac.uk.ebi.pmc.importer.import.BatchPmcImporter
import ac.uk.ebi.pmc.importer.import.FileDownloader
import ac.uk.ebi.pmc.importer.import.PmcImporter
import ac.uk.ebi.scheduler.properties.PmcImporterProperties
import com.mongodb.async.client.MongoClient
import mu.KotlinLogging
import org.litote.kmongo.async.KMongo
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

private val logger = KotlinLogging.logger {}

@Configuration
class AppConfig {

    @Bean
    @ConfigurationProperties("app.data")
    fun properties() = PmcImporterProperties()

    @Bean
    fun serializationService() = SerializationService()

    @Bean
    fun mongoClient(properties: PmcImporterProperties): MongoClient {
        return KMongo.createClient(properties.mongodbUri)
    }

    @Bean
    fun subRepository(client: MongoClient) = MongoRepository("eubioimag", client)

    @Bean
    fun submissionDocService(subRepository: MongoRepository, serializationService: SerializationService) =
        MongoDocService(subRepository, serializationService)

    @Bean
    fun fileDownloader(pmcApi: PmcApi, properties: PmcImporterProperties) = FileDownloader(properties, pmcApi)

    @Bean
    fun pmcImporter(
        submissionDocService: MongoDocService,
        serializationService: SerializationService,
        fileDownloader: FileDownloader
    ) = PmcImporter(submissionDocService, serializationService, fileDownloader)

    @Bean
    fun pmcBatchImporter(pmcImporter: PmcImporter) = BatchPmcImporter(pmcImporter)
}