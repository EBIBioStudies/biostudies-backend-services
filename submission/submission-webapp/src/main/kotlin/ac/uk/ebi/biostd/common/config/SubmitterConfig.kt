package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.common.config.SubmitterConfig.FilesHandlerConfig
import ac.uk.ebi.biostd.common.config.SubmitterConfig.ServiceConfig
import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.integration.SerializationConfig
import ac.uk.ebi.biostd.persistence.common.service.PersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionMetaQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestService
import ac.uk.ebi.biostd.submission.service.AccNoService
import ac.uk.ebi.biostd.submission.service.CollectionInfoService
import ac.uk.ebi.biostd.submission.service.ParentInfoService
import ac.uk.ebi.biostd.submission.service.TimesService
import ac.uk.ebi.biostd.submission.submitter.SubmissionSubmitter
import ac.uk.ebi.biostd.submission.util.AccNoPatternUtil
import ac.uk.ebi.biostd.submission.validator.collection.CollectionValidator
import ac.uk.ebi.biostd.submission.validator.collection.EuToxRiskValidator
import ebi.ac.uk.commons.http.JacksonFactory
import ebi.ac.uk.paths.SubmissionFolderResolver
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import org.springframework.beans.factory.BeanFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Lazy
import org.springframework.web.client.RestTemplate
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import java.nio.file.Paths

@Suppress("LongParameterList")
@Configuration
@Import(ServiceConfig::class, FilesHandlerConfig::class, SecurityBeansConfig::class)
class SubmitterConfig {
    @Bean
    fun submissionSubmitter(
        timesService: TimesService,
        accNoService: AccNoService,
        parentInfoService: ParentInfoService,
        collectionInfoService: CollectionInfoService,
        requestService: SubmissionRequestService,
        submissionQueryService: SubmissionMetaQueryService
    ) = SubmissionSubmitter(
        timesService,
        accNoService,
        parentInfoService,
        collectionInfoService,
        requestService,
        submissionQueryService)

    @Configuration
    class FilesHandlerConfig(private val appProperties: ApplicationProperties) {
        @Bean
        @Lazy
        fun folderResolver() = SubmissionFolderResolver(
            submissionFolder = Paths.get(appProperties.submissionPath),
            ftpFolder = Paths.get(appProperties.ftpPath))
    }

    @Configuration
    class SerializationConfiguration {
        @Bean
        fun serializationService() = SerializationConfig.serializationService()

        @Bean
        fun extSerializationService(): ExtSerializationService = ExtSerializationService()
    }

    @Configuration
    @Suppress("MagicNumber")
    class ServiceConfig(
        private val service: PersistenceService,
        private val queryService: SubmissionMetaQueryService,
        private val userPrivilegesService: IUserPrivilegesService
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
    class CollectionValidatorConfig {
        @Bean
        fun restTemplate(): RestTemplate = RestTemplate()

        @Bean(name = ["EuToxRiskValidator"])
        fun euToxRiskValidator(
            restTemplate: RestTemplate,
            applicationProperties: ApplicationProperties
        ): CollectionValidator = EuToxRiskValidator(restTemplate, applicationProperties.validator)
    }
}
