package ac.uk.ebi.pmc.config

import ac.uk.ebi.biostd.common.SerializationConfig
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.pmc.PmcTaskExecutor
import ac.uk.ebi.pmc.client.PmcApi
import ac.uk.ebi.pmc.load.LoaderConfig
import ac.uk.ebi.pmc.persistence.domain.ErrorsService
import ac.uk.ebi.pmc.persistence.domain.InputFilesService
import ac.uk.ebi.pmc.persistence.domain.SubmissionService
import ac.uk.ebi.pmc.persistence.repository.ErrorsDataRepository
import ac.uk.ebi.pmc.persistence.repository.InputFilesDataRepository
import ac.uk.ebi.pmc.persistence.repository.SubFileDataRepository
import ac.uk.ebi.pmc.persistence.repository.SubmissionDataRepository
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
        SubmitterConfig::class,
    ],
)
@EnableConfigurationProperties
class AppConfig {
    @Bean
    fun pmcTaskExecutor(
        properties: PmcImporterProperties,
        notificationSender: NotificationsSender,
    ) = PmcTaskExecutor(properties, notificationSender)

    @Bean
    fun serializationService(): SerializationService = SerializationConfig.serializationService()

    @Bean
    fun fileDownloader(pmcApi: PmcApi): FileDownloader = FileDownloader(pmcApi)

    @Bean
    fun submissionInitializer(serializationService: SerializationService): SubmissionInitializer =
        SubmissionInitializer(serializationService)

    @Bean
    fun submissionService(
        subRepository: SubmissionDataRepository,
        fileRepository: SubFileDataRepository,
        serializationService: SerializationService,
    ): SubmissionService = SubmissionService(subRepository, fileRepository, serializationService)

    @Bean
    fun errorsService(
        repository: ErrorsDataRepository,
        submissionService: SubmissionService,
    ): ErrorsService = ErrorsService(repository, submissionService)

    @Bean
    fun inputFilesService(repository: InputFilesDataRepository): InputFilesService = InputFilesService(repository)
}
