package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.common.config.SubmitterConfig.FilesHandlerConfig
import ac.uk.ebi.biostd.common.config.SubmitterConfig.ServiceConfig
import ac.uk.ebi.biostd.common.property.ApplicationProperties
import ac.uk.ebi.biostd.integration.SerializationConfig
import ac.uk.ebi.biostd.persistence.integration.PersistenceContext
import ac.uk.ebi.biostd.submission.handlers.FilesCopier
import ac.uk.ebi.biostd.submission.handlers.FilesHandler
import ac.uk.ebi.biostd.submission.handlers.FilesValidator
import ac.uk.ebi.biostd.submission.handlers.OutputFilesGenerator
import ac.uk.ebi.biostd.submission.service.AccNoService
import ac.uk.ebi.biostd.submission.service.ParentInfoService
import ac.uk.ebi.biostd.submission.service.ProjectService
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

@Configuration
@Import(ServiceConfig::class, FilesHandlerConfig::class, SecurityBeansConfig::class)
class SubmitterConfig {
    @Bean
    fun submissionSubmitter(
        timesService: TimesService,
        accNoService: AccNoService,
        parentInfoService: ParentInfoService,
        projectService: ProjectService,
        persistenceContext: PersistenceContext
    ) = SubmissionSubmitter(timesService, accNoService, parentInfoService, projectService, persistenceContext)

    @Bean
    fun projectService(
        persistenceContext: PersistenceContext,
        accNoPatternUtil: AccNoPatternUtil,
        privilegesService: IUserPrivilegesService
    ) = ProjectService(persistenceContext, accNoPatternUtil, privilegesService)

    @Configuration
    class FilesHandlerConfig(private val appProperties: ApplicationProperties) {
        @Bean
        @Lazy
        fun folderResolver() = SubmissionFolderResolver(Paths.get(appProperties.basepath))

        @Bean
        fun serializationService() = SerializationConfig.serializationService()

        @Bean
        fun filesHandler() = FilesHandler(filesValidator(), filesCopier(), outputFilesGenerator())

        @Bean
        fun outputFilesGenerator() = OutputFilesGenerator(folderResolver(), serializationService())

        @Bean
        fun filesCopier() = FilesCopier(folderResolver())

        @Bean
        fun filesValidator() = FilesValidator()
    }

    @Configuration
    @Suppress("MagicNumber")
    class ServiceConfig(
        private val context: PersistenceContext,
        private val userPrivilegesService: IUserPrivilegesService
    ) {
        @Bean
        fun accNoPatternUtil() = AccNoPatternUtil()

        @Bean
        fun accNoService() = AccNoService(context, accNoPatternUtil(), userPrivilegesService)

        @Bean
        fun parentInfoService() = ParentInfoService(context)

        @Bean
        fun projectValidationService() = ProjectService(context, accNoPatternUtil(), userPrivilegesService)

        @Bean
        fun timesService() = TimesService(context)
    }
}
