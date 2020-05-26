package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.common.config.SubmitterConfig.FilesHandlerConfig
import ac.uk.ebi.biostd.common.config.SubmitterConfig.ServiceConfig
import ac.uk.ebi.biostd.common.property.ApplicationProperties
import ac.uk.ebi.biostd.extended.ExtSubmissionSerializer
import ac.uk.ebi.biostd.integration.SerializationConfig
import ac.uk.ebi.biostd.persistence.integration.PersistenceContext
import ac.uk.ebi.biostd.persistence.integration.SubmissionQueryService
import ac.uk.ebi.biostd.submission.service.AccNoService
import ac.uk.ebi.biostd.submission.service.ParentInfoService
import ac.uk.ebi.biostd.submission.service.ProjectInfoService
import ac.uk.ebi.biostd.submission.service.TimesService
import ac.uk.ebi.biostd.submission.submitter.SubmissionSubmitter
import ac.uk.ebi.biostd.submission.util.AccNoPatternUtil
import ebi.ac.uk.paths.SubmissionFolderResolver
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Lazy
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
        projectInfoService: ProjectInfoService,
        persistenceContext: PersistenceContext,
        submissionQueryService: SubmissionQueryService
    ) = SubmissionSubmitter(
        timesService,
        accNoService,
        parentInfoService,
        projectInfoService,
        persistenceContext,
        submissionQueryService)

    @Configuration
    class FilesHandlerConfig(private val appProperties: ApplicationProperties) {
        @Bean
        @Lazy
        fun folderResolver() = SubmissionFolderResolver(Paths.get(appProperties.basepath))
    }

    @Configuration
    class SerializationConfiguration {
        @Bean
        fun serializationService() = SerializationConfig.serializationService()

        @Bean
        fun extSubmissionSerializer(): ExtSubmissionSerializer = SerializationConfig.extSubmissionSerializer()
    }

    @Configuration
    @Suppress("MagicNumber")
    class ServiceConfig(
        private val context: PersistenceContext,
        private val queryService: SubmissionQueryService,
        private val userPrivilegesService: IUserPrivilegesService
    ) {
        @Bean
        fun accNoPatternUtil() = AccNoPatternUtil()

        @Bean
        fun accNoService() = AccNoService(context, queryService, accNoPatternUtil(), userPrivilegesService)

        @Bean
        fun parentInfoService() = ParentInfoService(queryService)

        @Bean
        fun projectInfoService() = ProjectInfoService(context, queryService, accNoPatternUtil(), userPrivilegesService)

        @Bean
        fun timesService() = TimesService(queryService)
    }
}
