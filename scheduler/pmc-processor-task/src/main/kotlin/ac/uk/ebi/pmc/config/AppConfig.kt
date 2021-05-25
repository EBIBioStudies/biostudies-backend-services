package ac.uk.ebi.pmc.config

import ac.uk.ebi.biostd.integration.SerializationConfig
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.pmc.PmcTaskExecutor
import ac.uk.ebi.pmc.client.PmcApi
import ac.uk.ebi.pmc.load.LoaderConfig
import ac.uk.ebi.pmc.persistence.ErrorsDocService
import ac.uk.ebi.pmc.persistence.InputFilesDocService
import ac.uk.ebi.pmc.persistence.SubmissionDocService
import ac.uk.ebi.pmc.persistence.repository.ErrorsRepository
import ac.uk.ebi.pmc.persistence.repository.InputFileRepository
import ac.uk.ebi.pmc.persistence.repository.SubFileRepository
import ac.uk.ebi.pmc.persistence.repository.SubmissionRepository
import ac.uk.ebi.pmc.process.ProcessorConfig
import ac.uk.ebi.pmc.process.util.FileDownloader
import ac.uk.ebi.pmc.process.util.SubmissionInitializer
import ac.uk.ebi.pmc.submit.SubmitterConfig
import ac.uk.ebi.scheduler.properties.PmcImporterProperties
import ebi.ac.uk.commons.http.slack.NotificationsSender
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(
    value = [
        PropConfig::class,
        PersistenceConfig::class,
        NotificationsConfig::class,
        WebConfig::class,
        LoaderConfig::class,
        ProcessorConfig::class,
        SubmitterConfig::class
    ]
)
@EnableConfigurationProperties
class AppConfig {

    @Bean
    fun pmcTaskExecutor(properties: PmcImporterProperties, notificationSender: NotificationsSender) =
        PmcTaskExecutor(properties, notificationSender)

    @Bean
    fun serializationService() = SerializationConfig.serializationService()

    @Bean
    fun inputFilesDocService(inputFileRepository: InputFileRepository) = InputFilesDocService(inputFileRepository)

    @Bean
    fun errorsDocService(errorsRepository: ErrorsRepository, submissionRepository: SubmissionRepository) =
        ErrorsDocService(errorsRepository, submissionRepository)

    @Bean
    fun submissionDocService(
        submissionRepository: SubmissionRepository,
        submissionFileRepository: SubFileRepository,
        serializationService: SerializationService
    ) = SubmissionDocService(submissionRepository, submissionFileRepository, serializationService)

    @Bean
    fun fileDownloader(pmcApi: PmcApi, properties: PmcImporterProperties) = FileDownloader(properties, pmcApi)

    @Bean
    fun submissionInitializer(serializationService: SerializationService) = SubmissionInitializer(serializationService)
}
