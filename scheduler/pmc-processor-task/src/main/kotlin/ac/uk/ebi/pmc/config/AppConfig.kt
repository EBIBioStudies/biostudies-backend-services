package ac.uk.ebi.pmc.config

import ac.uk.ebi.biostd.SerializationService
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.pmc.client.PmcApi
import ac.uk.ebi.pmc.data.MongoDocService
import ac.uk.ebi.pmc.data.MongoRepository
import ac.uk.ebi.pmc.submit.PmcBatchSubmitter
import ac.uk.ebi.pmc.submit.PmcSubmitter
import ac.uk.ebi.pmc.import.PmcBatchImporter
import ac.uk.ebi.pmc.import.FileDownloader
import ac.uk.ebi.pmc.import.PmcImporter
import ac.uk.ebi.scheduler.properties.PmcImporterProperties
import com.mongodb.async.client.MongoClient
import org.litote.kmongo.async.KMongo
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

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
    fun bioWebClient() =
        BioWebClient.create("http://localhost:8080", "theToken")

    @Bean
    fun pmcSubmitter(
        bioWebClient: BioWebClient,
        submissionDocService: MongoDocService,
        serializationService: SerializationService
    ) = PmcSubmitter(bioWebClient, submissionDocService, serializationService)

    @Bean
    fun pmcBatchImporter(pmcImporter: PmcImporter) = PmcBatchImporter(pmcImporter)

    @Bean
    fun pmcBatchSubmitter(pmcSubmitter: PmcSubmitter) = PmcBatchSubmitter(pmcSubmitter)
}
