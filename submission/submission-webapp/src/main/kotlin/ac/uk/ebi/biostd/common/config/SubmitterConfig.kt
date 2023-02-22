package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.common.config.SubmitterConfig.FilesHandlerConfig
import ac.uk.ebi.biostd.common.config.SubmitterConfig.ServiceConfig
import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.persistence.common.service.PersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionDraftPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionMetaQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestFilesPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.persistence.doc.integration.SerializationConfiguration
import ac.uk.ebi.biostd.persistence.filesystem.api.FileStorageService
import ac.uk.ebi.biostd.persistence.filesystem.pagetab.PageTabService
import ac.uk.ebi.biostd.submission.service.AccNoService
import ac.uk.ebi.biostd.submission.service.CollectionInfoService
import ac.uk.ebi.biostd.submission.service.FileSourcesService
import ac.uk.ebi.biostd.submission.service.ParentInfoService
import ac.uk.ebi.biostd.submission.service.TimesService
import ac.uk.ebi.biostd.submission.submitter.ExtSubmissionSubmitter
import ac.uk.ebi.biostd.submission.submitter.SubmissionProcessor
import ac.uk.ebi.biostd.submission.submitter.SubmissionSubmitter
import ac.uk.ebi.biostd.submission.submitter.request.SubmissionRequestCleaner
import ac.uk.ebi.biostd.submission.submitter.request.SubmissionRequestFinalizer
import ac.uk.ebi.biostd.submission.submitter.request.SubmissionRequestIndexer
import ac.uk.ebi.biostd.submission.submitter.request.SubmissionRequestLoader
import ac.uk.ebi.biostd.submission.submitter.request.SubmissionRequestProcessor
import ac.uk.ebi.biostd.submission.submitter.request.SubmissionRequestReleaser
import ac.uk.ebi.biostd.submission.submitter.request.SubmissionRequestSaver
import ac.uk.ebi.biostd.submission.util.AccNoPatternUtil
import ac.uk.ebi.biostd.submission.validator.collection.CollectionValidator
import ac.uk.ebi.biostd.submission.validator.collection.EuToxRiskValidator
import ac.uk.ebi.biostd.submission.validator.filelist.FileListValidator
import ebi.ac.uk.extended.mapping.from.ToExtFileListMapper
import ebi.ac.uk.extended.mapping.from.ToExtSectionMapper
import ebi.ac.uk.paths.SubmissionFolderResolver
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import org.springframework.beans.factory.BeanFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Lazy
import org.springframework.web.client.RestTemplate
import uk.ac.ebi.events.service.EventsPublisherService
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.FileProcessingService
import uk.ac.ebi.serialization.common.FilesResolver
import java.nio.file.Paths

@Suppress("LongParameterList")
@Configuration
@Import(ServiceConfig::class, FilesHandlerConfig::class, SecurityBeansConfig::class, SerializationConfiguration::class)
class SubmitterConfig {
    @Bean
    fun requestIndexer(
        serializationService: ExtSerializationService,
        requestService: SubmissionRequestPersistenceService,
        filesRequestService: SubmissionRequestFilesPersistenceService,
    ): SubmissionRequestIndexer = SubmissionRequestIndexer(serializationService, requestService, filesRequestService)

    @Bean
    fun requestLoader(
        filesRequestService: SubmissionRequestFilesPersistenceService,
        requestService: SubmissionRequestPersistenceService,
    ): SubmissionRequestLoader = SubmissionRequestLoader(filesRequestService, requestService)

    @Bean
    fun requestSaver(
        requestService: SubmissionRequestPersistenceService,
        fileProcessingService: FileProcessingService,
        persistenceService: SubmissionPersistenceService,
        filesRequestService: SubmissionRequestFilesPersistenceService,
        eventsPublisherService: EventsPublisherService,
    ): SubmissionRequestSaver {
        return SubmissionRequestSaver(
            requestService,
            fileProcessingService,
            persistenceService,
            filesRequestService,
            eventsPublisherService
        )
    }

    @Bean
    fun requestProcessor(
        storageService: FileStorageService,
        requestService: SubmissionRequestPersistenceService,
        filesRequestService: SubmissionRequestFilesPersistenceService,
    ): SubmissionRequestProcessor = SubmissionRequestProcessor(
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
        submissionPersistenceService: SubmissionPersistenceService,
        filesRequestService: SubmissionRequestFilesPersistenceService,
    ): SubmissionRequestReleaser = SubmissionRequestReleaser(
        fileStorageService,
        serializationService,
        submissionPersistenceQueryService,
        submissionPersistenceService,
        requestService,
        filesRequestService
    )

    @Bean
    fun submissionCleaner(
        storageService: FileStorageService,
        serializationService: ExtSerializationService,
        queryService: SubmissionPersistenceQueryService,
        requestService: SubmissionRequestPersistenceService,
        filesRequestService: SubmissionRequestFilesPersistenceService,
    ): SubmissionRequestCleaner = SubmissionRequestCleaner(
        storageService,
        serializationService,
        queryService,
        requestService,
        filesRequestService,
    )

    @Bean
    fun submissionRequestFinalizer(
        storageService: FileStorageService,
        serializationService: ExtSerializationService,
        queryService: SubmissionPersistenceQueryService,
        requestService: SubmissionRequestPersistenceService,
    ): SubmissionRequestFinalizer = SubmissionRequestFinalizer(
        storageService,
        serializationService,
        queryService,
        requestService,
    )

    @Bean
    fun extSubmissionSubmitter(
        pageTabService: PageTabService,
        requestService: SubmissionRequestPersistenceService,
        persistenceService: SubmissionPersistenceService,
        requestIndexer: SubmissionRequestIndexer,
        requestLoader: SubmissionRequestLoader,
        requestProcessor: SubmissionRequestProcessor,
        submissionReleaser: SubmissionRequestReleaser,
        submissionCleaner: SubmissionRequestCleaner,
        submissionSaver: SubmissionRequestSaver,
        submissionFinalizer: SubmissionRequestFinalizer,
    ): ExtSubmissionSubmitter = ExtSubmissionSubmitter(
        pageTabService,
        requestService,
        persistenceService,
        requestIndexer,
        requestLoader,
        requestProcessor,
        submissionReleaser,
        submissionCleaner,
        submissionSaver,
        submissionFinalizer,
    )

    @Bean
    fun submissionSubmitter(
        extSubmissionSubmitter: ExtSubmissionSubmitter,
        submissionProcessor: SubmissionProcessor,
        parentInfoService: ParentInfoService,
        draftService: SubmissionDraftPersistenceService,
    ): SubmissionSubmitter = SubmissionSubmitter(
        extSubmissionSubmitter,
        submissionProcessor,
        parentInfoService,
        draftService,
    )

    @Bean
    fun submissionProcessor(
        persistenceService: SubmissionPersistenceService,
        timesService: TimesService,
        accNoService: AccNoService,
        parentInfoService: ParentInfoService,
        collectionInfoService: CollectionInfoService,
        properties: ApplicationProperties,
        toExtSectionMapper: ToExtSectionMapper,
        fileListValidator: FileListValidator,
    ): SubmissionProcessor =
        SubmissionProcessor(
            persistenceService,
            timesService,
            accNoService,
            parentInfoService,
            collectionInfoService,
            properties,
            toExtSectionMapper,
            fileListValidator,
        )

    @Configuration
    class ToExtendedConfiguration {
        @Bean
        fun toExtSection(toExtFileListMapper: ToExtFileListMapper): ToExtSectionMapper =
            ToExtSectionMapper(toExtFileListMapper)

        @Bean
        fun toExtFileList(
            extSerializationService: ExtSerializationService,
            serializationService: SerializationService,
            filesResolver: FilesResolver,
        ): ToExtFileListMapper =
            ToExtFileListMapper(extSerializationService, serializationService, filesResolver)
    }

    @Configuration
    class FilesHandlerConfig(private val appProperties: ApplicationProperties) {
        @Bean
        @Lazy
        fun folderResolver() = SubmissionFolderResolver(
            submissionFolder = Paths.get(appProperties.submissionPath),
            ftpFolder = Paths.get(appProperties.ftpPath)
        )
    }

    @Configuration
    @Suppress("MagicNumber")
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
        fun accNoService() = AccNoService(service, accNoPatternUtil(), userPrivilegesService, properties.subBasePath)

        @Bean
        fun parentInfoService(beanFactory: BeanFactory) = ParentInfoService(beanFactory, queryService)

        @Bean
        fun projectInfoService() = CollectionInfoService(service, accNoPatternUtil(), userPrivilegesService)

        @Bean
        fun timesService() = TimesService()
    }

    @Configuration
    class ValidatorConfig {
        @Bean
        fun restTemplate(): RestTemplate = RestTemplate()

        @Bean
        fun fileListValidator(
            fileSourcesService: FileSourcesService,
            serializationService: SerializationService,
            submissionQueryService: SubmissionPersistenceQueryService,
        ): FileListValidator = FileListValidator(fileSourcesService, serializationService, submissionQueryService)

        @Bean(name = ["EuToxRiskValidator"])
        fun euToxRiskValidator(
            restTemplate: RestTemplate,
            applicationProperties: ApplicationProperties,
        ): CollectionValidator = EuToxRiskValidator(restTemplate, applicationProperties.validator)
    }
}
