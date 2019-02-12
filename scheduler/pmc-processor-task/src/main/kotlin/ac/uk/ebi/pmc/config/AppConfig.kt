package ac.uk.ebi.pmc.config

import ac.uk.ebi.biostd.SerializationService
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.pmc.client.PmcApi
import ac.uk.ebi.pmc.load.PmcLoader
import ac.uk.ebi.pmc.load.PmcSubmissionLoader
import ac.uk.ebi.pmc.persistence.MongoDocService
import ac.uk.ebi.pmc.persistence.SubmissionDocService
import ac.uk.ebi.pmc.persistence.repository.ErrorsRepository
import ac.uk.ebi.pmc.persistence.repository.InputFileRepository
import ac.uk.ebi.pmc.persistence.repository.SubFileRepository
import ac.uk.ebi.pmc.persistence.repository.SubmissionRepository
import ac.uk.ebi.pmc.process.FileDownloader
import ac.uk.ebi.pmc.process.PmcProcessor
import ac.uk.ebi.pmc.process.PmcSubmissionProcessor
import ac.uk.ebi.pmc.submit.PmcBatchSubmitter
import ac.uk.ebi.pmc.submit.PmcSubmitter
import ac.uk.ebi.scheduler.properties.PmcImporterProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(value = [PersistenceConfig::class])
class AppConfig {

    @Bean
    @ConfigurationProperties("app.data")
    fun properties() = PmcImporterProperties()

    @Bean
    fun serializationService() = SerializationService()

    @Bean
    fun mongoDocService(
        errorsRepository: ErrorsRepository,
        submissionRepository: SubmissionRepository,
        submissionFileRepository: SubFileRepository,
        inputFileRepository: InputFileRepository
    ) = MongoDocService(submissionRepository, errorsRepository, inputFileRepository)

    @Bean
    fun submissionDocService(
        submissionRepository: SubmissionRepository,
        submissionFileRepository: SubFileRepository,
        serializationService: SerializationService
    ) = SubmissionDocService(submissionRepository, submissionFileRepository, serializationService)

    @Bean
    fun fileDownloader(pmcApi: PmcApi, properties: PmcImporterProperties) = FileDownloader(properties, pmcApi)

    @Bean
    fun pmcImporter(
        docService: MongoDocService,
        serializationService: SerializationService,
        submissionDocService: SubmissionDocService,
        fileDownloader: FileDownloader
    ) = PmcProcessor(docService, serializationService, submissionDocService, fileDownloader)

    @Bean
    fun bioWebClient(properties: PmcImporterProperties) =
        SecurityWebClient
                    .create(properties.bioStudiesUrl)
                    .getAuthenticatedClient(properties.bioStudiesUser, properties.bioStudiesPassword)

    @Bean
    fun pmcSubmitter(
        bioWebClient: BioWebClient,
        docService: MongoDocService,
        submissionDocService: SubmissionDocService
    ) = PmcSubmitter(bioWebClient, docService, submissionDocService)

    @Bean
    fun pmcBatchImporter(pmcImporter: PmcProcessor) = PmcSubmissionProcessor(pmcImporter)

    @Bean
    fun pmcBatchSubmitter(pmcSubmitter: PmcSubmitter) = PmcBatchSubmitter(pmcSubmitter)

    @Bean
    fun pmcSubmissionLoader(
        docService: MongoDocService,
        submissionDocService: SubmissionDocService,
        serializationService: SerializationService
    ) = PmcSubmissionLoader(serializationService, docService, submissionDocService)

    @Bean
    fun pmcSubmissionLoader(pmcSubmissionLoader: PmcSubmissionLoader) = PmcLoader(pmcSubmissionLoader)
}
