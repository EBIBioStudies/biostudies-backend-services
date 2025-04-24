package ac.uk.ebi.biostd.submission.config

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.persistence.common.service.PersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionMetaQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestFilesPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.persistence.doc.integration.ExternalConfig
import ac.uk.ebi.biostd.persistence.doc.integration.SerializationConfiguration
import ac.uk.ebi.biostd.persistence.filesystem.api.FileStorageService
import ac.uk.ebi.biostd.persistence.filesystem.pagetab.PageTabService
import ac.uk.ebi.biostd.submission.config.SubmitterConfig.FilesHandlerConfig
import ac.uk.ebi.biostd.submission.config.SubmitterConfig.ServiceConfig
import ac.uk.ebi.biostd.submission.domain.extended.ExtSubmissionQueryService
import ac.uk.ebi.biostd.submission.domain.request.SubmissionRequestCleanIndexer
import ac.uk.ebi.biostd.submission.domain.request.SubmissionRequestCleaner
import ac.uk.ebi.biostd.submission.domain.request.SubmissionRequestIndexer
import ac.uk.ebi.biostd.submission.domain.request.SubmissionRequestLoader
import ac.uk.ebi.biostd.submission.domain.request.SubmissionRequestProcessor
import ac.uk.ebi.biostd.submission.domain.request.SubmissionRequestReleaser
import ac.uk.ebi.biostd.submission.domain.request.SubmissionRequestSaver
import ac.uk.ebi.biostd.submission.domain.request.SubmissionRequestValidator
import ac.uk.ebi.biostd.submission.domain.submission.SubFolderResolver
import ac.uk.ebi.biostd.submission.domain.submission.SubmissionProcessor
import ac.uk.ebi.biostd.submission.domain.submission.SubmissionSubmitter
import ac.uk.ebi.biostd.submission.domain.submitter.ExtSubmissionSubmitter
import ac.uk.ebi.biostd.submission.domain.submitter.LocalExtSubmissionSubmitter
import ac.uk.ebi.biostd.submission.service.AccNoService
import ac.uk.ebi.biostd.submission.service.CollectionProcessor
import ac.uk.ebi.biostd.submission.service.DoiService
import ac.uk.ebi.biostd.submission.service.FileSourcesService
import ac.uk.ebi.biostd.submission.service.TimesService
import ac.uk.ebi.biostd.submission.stats.SubmissionStatsService
import ac.uk.ebi.biostd.submission.util.AccNoPatternUtil
import ac.uk.ebi.biostd.submission.validator.collection.CollectionValidationService
import ac.uk.ebi.biostd.submission.validator.collection.CollectionValidator
import ac.uk.ebi.biostd.submission.validator.collection.EuToxRiskValidator
import ac.uk.ebi.biostd.submission.validator.filelist.FileListValidator
import ebi.ac.uk.extended.mapping.from.ToExtFileListMapper
import ebi.ac.uk.extended.mapping.from.ToExtSectionMapper
import ebi.ac.uk.paths.SubmissionFolderResolver
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import org.springframework.beans.factory.BeanFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.web.reactive.function.client.WebClient
import uk.ac.ebi.events.service.EventsPublisherService
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.FileProcessingService
import uk.ac.ebi.fire.client.integration.web.FireClient
import uk.ac.ebi.serialization.common.FilesResolver
import java.io.File

@Suppress("LongParameterList", "TooManyFunctions")
@Configuration
@Import(
    ServiceConfig::class,
    FilesHandlerConfig::class,
    SecurityConfig::class,
    SerializationConfiguration::class,
    ExternalConfig::class,
)
class SubmitterConfig(
    private val properties: ApplicationProperties,
) {
    @Bean
    fun requestIndexer(
        serializationService: ExtSerializationService,
        requestService: SubmissionRequestPersistenceService,
        filesRequestService: SubmissionRequestFilesPersistenceService,
    ): SubmissionRequestIndexer =
        SubmissionRequestIndexer(
            serializationService,
            requestService,
            filesRequestService,
        )

    @Bean
    fun requestToCleanIndexer(
        serializationService: ExtSerializationService,
        queryService: SubmissionPersistenceQueryService,
        filesRequestService: SubmissionRequestFilesPersistenceService,
        requestService: SubmissionRequestPersistenceService,
    ): SubmissionRequestCleanIndexer =
        SubmissionRequestCleanIndexer(
            serializationService,
            queryService,
            filesRequestService,
            requestService,
        )

    @Bean
    fun requestValidator(
        userPrivilegesService: IUserPrivilegesService,
        queryService: SubmissionPersistenceQueryService,
        requestService: SubmissionRequestPersistenceService,
        applicationProperties: ApplicationProperties,
    ): SubmissionRequestValidator =
        SubmissionRequestValidator(
            userPrivilegesService,
            queryService,
            requestService,
            applicationProperties.security,
        )

    @Bean
    fun requestLoader(
        filesRequestService: SubmissionRequestFilesPersistenceService,
        requestService: SubmissionRequestPersistenceService,
    ): SubmissionRequestLoader =
        SubmissionRequestLoader(
            properties.persistence.concurrency,
            File(properties.fire.tempDirPath),
            filesRequestService,
            requestService,
        )

    @Bean
    fun requestSaver(
        requestService: SubmissionRequestPersistenceService,
        fileProcessingService: FileProcessingService,
        persistenceService: SubmissionPersistenceService,
        filesRequestService: SubmissionRequestFilesPersistenceService,
    ): SubmissionRequestSaver =
        SubmissionRequestSaver(
            requestService,
            fileProcessingService,
            persistenceService,
            filesRequestService,
        )

    @Bean
    fun requestProcessor(
        storageService: FileStorageService,
        requestService: SubmissionRequestPersistenceService,
        filesRequestService: SubmissionRequestFilesPersistenceService,
    ): SubmissionRequestProcessor =
        SubmissionRequestProcessor(
            properties.persistence.concurrency,
            storageService,
            requestService,
            filesRequestService,
        )

    @Bean
    fun submissionReleaser(
        fileStorageService: FileStorageService,
        serializationService: ExtSerializationService,
        requestService: SubmissionRequestPersistenceService,
        submissionPersistenceQueryService: SubmissionPersistenceQueryService,
        filesRequestService: SubmissionRequestFilesPersistenceService,
    ): SubmissionRequestReleaser =
        SubmissionRequestReleaser(
            properties.persistence.concurrency,
            fileStorageService,
            serializationService,
            submissionPersistenceQueryService,
            requestService,
            filesRequestService,
        )

    @Bean
    fun submissionCleaner(
        queryService: SubmissionPersistenceQueryService,
        storageService: FileStorageService,
        requestService: SubmissionRequestPersistenceService,
        filesRequestService: SubmissionRequestFilesPersistenceService,
    ): SubmissionRequestCleaner =
        SubmissionRequestCleaner(
            properties.persistence.concurrency,
            queryService,
            storageService,
            requestService,
            filesRequestService,
        )

    @Bean
    @ConditionalOnMissingBean(ExtSubmissionSubmitter::class)
    fun localExtSubmissionSubmitter(
        appProperties: ApplicationProperties,
        pageTabService: PageTabService,
        requestService: SubmissionRequestPersistenceService,
        persistenceService: SubmissionPersistenceService,
        submissionQueryService: ExtSubmissionQueryService,
        requestIndexer: SubmissionRequestIndexer,
        requestLoader: SubmissionRequestLoader,
        requestToCleanIndexed: SubmissionRequestCleanIndexer,
        requestValidator: SubmissionRequestValidator,
        requestProcessor: SubmissionRequestProcessor,
        submissionReleaser: SubmissionRequestReleaser,
        submissionCleaner: SubmissionRequestCleaner,
        submissionSaver: SubmissionRequestSaver,
        eventsPublisherService: EventsPublisherService,
        submissionStatsService: SubmissionStatsService,
    ): ExtSubmissionSubmitter =
        LocalExtSubmissionSubmitter(
            appProperties,
            pageTabService,
            requestService,
            persistenceService,
            requestIndexer,
            requestLoader,
            requestToCleanIndexed,
            requestValidator,
            requestProcessor,
            submissionReleaser,
            submissionCleaner,
            submissionSaver,
            submissionQueryService,
            eventsPublisherService,
            submissionStatsService,
        )

    @Bean
    fun submissionSubmitter(
        extSubmissionSubmitter: ExtSubmissionSubmitter,
        submissionProcessor: SubmissionProcessor,
        collectionValidationService: CollectionValidationService,
        requestPersistenceService: SubmissionRequestPersistenceService,
    ): SubmissionSubmitter =
        SubmissionSubmitter(
            extSubmissionSubmitter,
            submissionProcessor,
            collectionValidationService,
            requestPersistenceService,
        )

    @Bean
    fun submissionProcessor(
        doiService: DoiService,
        timesService: TimesService,
        collectionProcessor: CollectionProcessor,
        properties: ApplicationProperties,
        toExtSectionMapper: ToExtSectionMapper,
    ): SubmissionProcessor =
        SubmissionProcessor(
            doiService,
            timesService,
            collectionProcessor,
            properties,
            toExtSectionMapper,
        )

    @Configuration
    class ToExtendedConfiguration {
        @Bean
        fun toExtSection(toExtFileListMapper: ToExtFileListMapper): ToExtSectionMapper = ToExtSectionMapper(toExtFileListMapper)

        @Bean
        fun toExtFileList(
            extSerializationService: ExtSerializationService,
            serializationService: SerializationService,
            filesResolver: FilesResolver,
        ): ToExtFileListMapper = ToExtFileListMapper(extSerializationService, serializationService, filesResolver)
    }

    @Configuration
    class FilesHandlerConfig {
        @Bean
        fun folderResolver(appProperties: ApplicationProperties): SubmissionFolderResolver = SubFolderResolver(appProperties)
    }

    @Configuration
    @EnableConfigurationProperties
    class ServiceConfig(
        private val service: PersistenceService,
        private val queryService: SubmissionMetaQueryService,
        private val userPrivilegesService: IUserPrivilegesService,
        private val properties: ApplicationProperties,
    ) {
        @Bean
        fun accNoPatternUtil() = AccNoPatternUtil()

        @Bean
        fun doiService(webClient: WebClient) = DoiService(webClient, properties.doi)

        @Bean
        fun accNoService() = AccNoService(service, accNoPatternUtil(), userPrivilegesService, properties.subBasePath)

        @Bean
        fun parentInfoService(beanFactory: BeanFactory) = CollectionValidationService(beanFactory, queryService)

        @Bean
        fun projectInfoService() = CollectionProcessor(service, accNoPatternUtil(), userPrivilegesService)

        @Bean
        fun timesService() = TimesService(userPrivilegesService)
    }

    @Configuration
    class ValidatorConfig {
        @Bean
        fun webClient(): WebClient = WebClient.builder().build()

        @Bean
        fun fileListValidator(
            fileSourcesService: FileSourcesService,
            serializationService: SerializationService,
            submissionQueryService: SubmissionPersistenceQueryService,
        ): FileListValidator = FileListValidator(fileSourcesService, serializationService, submissionQueryService)

        @Bean(name = ["EuToxRiskValidator"])
        fun euToxRiskValidator(
            client: WebClient,
            applicationProperties: ApplicationProperties,
            fireClient: FireClient,
        ): CollectionValidator = EuToxRiskValidator(client, applicationProperties.validator, fireClient)
    }
}
