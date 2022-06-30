package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.common.config.SubmitterConfig.FilesHandlerConfig
import ac.uk.ebi.biostd.common.config.SubmitterConfig.ServiceConfig
import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.persistence.common.service.PersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionDraftService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionMetaQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.doc.integration.SerializationConfiguration
import ac.uk.ebi.biostd.persistence.filesystem.service.FileSystemService
import ac.uk.ebi.biostd.submission.service.AccNoService
import ac.uk.ebi.biostd.submission.service.CollectionInfoService
import ac.uk.ebi.biostd.submission.service.ParentInfoService
import ac.uk.ebi.biostd.submission.service.TimesService
import ac.uk.ebi.biostd.submission.submitter.ExtSubmissionSubmitter
import ac.uk.ebi.biostd.submission.submitter.SubmissionSubmitter
import ac.uk.ebi.biostd.submission.submitter.request.SubmissionRequestLoader
import ac.uk.ebi.biostd.submission.submitter.request.SubmissionRequestProcessor
import ac.uk.ebi.biostd.submission.util.AccNoPatternUtil
import ac.uk.ebi.biostd.submission.validator.collection.CollectionValidator
import ac.uk.ebi.biostd.submission.validator.collection.EuToxRiskValidator
import ac.uk.ebi.biostd.submission.validator.filelist.FileListValidator
import ebi.ac.uk.extended.mapping.from.ToExtFileListMapper
import ebi.ac.uk.extended.mapping.from.ToExtSectionMapper
import ebi.ac.uk.paths.SubmissionFolderResolver
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import org.springframework.beans.factory.BeanFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Lazy
import org.springframework.web.client.RestTemplate
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.FileProcessingService
import uk.ac.ebi.serialization.common.FilesResolver
import java.nio.file.Paths

@Suppress("LongParameterList")
@Configuration
@Import(ServiceConfig::class, FilesHandlerConfig::class, SecurityBeansConfig::class, SerializationConfiguration::class)
class SubmitterConfig {
    @Bean
    fun requestLoader(
        submissionPersistenceQueryService: SubmissionPersistenceQueryService,
        fileProcessingService: FileProcessingService,
    ): SubmissionRequestLoader = SubmissionRequestLoader(
        submissionPersistenceQueryService,
        fileProcessingService
    )

    @Bean
    fun requestProcessor(
        systemService: FileSystemService,
        submissionPersistenceService: SubmissionPersistenceService,
    ): SubmissionRequestProcessor = SubmissionRequestProcessor(
        systemService,
        submissionPersistenceService
    )

    @Bean
    fun extSubmissionSubmitter(
        persistenceService: SubmissionPersistenceService,
        submissionDraftService: SubmissionDraftService,
        requestLoader: SubmissionRequestLoader,
        requestProcessor: SubmissionRequestProcessor,
    ) = ExtSubmissionSubmitter(
        persistenceService,
        submissionDraftService,
        requestLoader,
        requestProcessor
    )

    @Bean
    fun submissionSubmitter(
        extSubmissionSubmitter: ExtSubmissionSubmitter,
        persistenceService: SubmissionPersistenceService,
        timesService: TimesService,
        accNoService: AccNoService,
        parentInfoService: ParentInfoService,
        collectionInfoService: CollectionInfoService,
        queryService: SubmissionMetaQueryService,
        properties: ApplicationProperties,
        toExtSectionMapper: ToExtSectionMapper,
    ) = SubmissionSubmitter(
        extSubmissionSubmitter,
        persistenceService,
        timesService,
        accNoService,
        parentInfoService,
        collectionInfoService,
        queryService,
        properties,
        toExtSectionMapper
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
    class ServiceConfig(
        private val service: PersistenceService,
        private val queryService: SubmissionMetaQueryService,
        private val userPrivilegesService: IUserPrivilegesService,
    ) {
        @Bean
        fun accNoPatternUtil() = AccNoPatternUtil()

        @Bean
        fun accNoService() = AccNoService(service, accNoPatternUtil(), userPrivilegesService)

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
            serializationService: SerializationService,
        ): FileListValidator = FileListValidator(serializationService)

        @Bean(name = ["EuToxRiskValidator"])
        fun euToxRiskValidator(
            restTemplate: RestTemplate,
            applicationProperties: ApplicationProperties,
        ): CollectionValidator = EuToxRiskValidator(restTemplate, applicationProperties.validator)
    }
}
