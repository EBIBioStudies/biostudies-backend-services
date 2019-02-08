package ac.uk.ebi.pmc.config

import ac.uk.ebi.biostd.SerializationService
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.pmc.client.PmcApi
import ac.uk.ebi.pmc.data.MongoDocService
import ac.uk.ebi.pmc.data.ext.getCollection
import ac.uk.ebi.pmc.data.repository.ErrorsRepository
import ac.uk.ebi.pmc.data.repository.SubFileRepository
import ac.uk.ebi.pmc.data.repository.SubRepository
import ac.uk.ebi.pmc.download.FileDownloader
import ac.uk.ebi.pmc.download.PmcProcessor
import ac.uk.ebi.pmc.download.PmcSubProcessor
import ac.uk.ebi.pmc.load.PmcSubmissionLoader
import ac.uk.ebi.pmc.submit.PmcBatchSubmitter
import ac.uk.ebi.pmc.submit.PmcSubmitter
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
    fun mongoClient(properties: PmcImporterProperties) = KMongo.createClient(properties.mongodbUri)

    @Bean
    fun errorsRepository(client: MongoClient) = ErrorsRepository(client.getCollection("eubioimag", "errors"))

    @Bean
    fun submissionRepository(client: MongoClient) = SubRepository(client.getCollection("eubioimag", "submissions"))

    @Bean
    fun submissionFileRepository(client: MongoClient) = SubFileRepository(client.getCollection("eubioimag", "files"))

    @Bean
    fun submissionDocService(
        serializationService: SerializationService,
        errorsRepository: ErrorsRepository,
        submissionRepository: SubRepository,
        submissionFileRepository: SubFileRepository
    ) =
        MongoDocService(submissionRepository, errorsRepository, submissionFileRepository, serializationService)

    @Bean
    fun fileDownloader(pmcApi: PmcApi, properties: PmcImporterProperties) = FileDownloader(properties, pmcApi)

    @Bean
    fun pmcImporter(
        submissionDocService: MongoDocService,
        serializationService: SerializationService,
        fileDownloader: FileDownloader
    ) = PmcProcessor(submissionDocService, serializationService, fileDownloader)

    @Bean
    fun bioWebClient(properties: PmcImporterProperties) =
            SecurityWebClient
                    .create(properties.bioStudiesUrl)
                    .getAuthenticatedClient(properties.bioStudiesUser, properties.bioStudiesPassword)

    @Bean
    fun pmcSubmitter(bioWebClient: BioWebClient, submissionDocService: MongoDocService) =
        PmcSubmitter(bioWebClient, submissionDocService)

    @Bean
    fun pmcBatchImporter(pmcImporter: PmcProcessor) = PmcSubProcessor(pmcImporter)

    @Bean
    fun pmcBatchSubmitter(pmcSubmitter: PmcSubmitter) = PmcBatchSubmitter(pmcSubmitter)

    @Bean
    fun pmcSubmissionLoader() = PmcSubmissionLoader()
}
