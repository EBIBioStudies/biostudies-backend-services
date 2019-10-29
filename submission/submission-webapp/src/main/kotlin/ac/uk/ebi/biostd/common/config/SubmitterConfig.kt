package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.common.config.SubmitterConfig.FilesHandlerConfig
import ac.uk.ebi.biostd.common.config.SubmitterConfig.ProcessorConfig
import ac.uk.ebi.biostd.common.config.SubmitterConfig.ValidatorConfig
import ac.uk.ebi.biostd.common.property.ApplicationProperties
import ac.uk.ebi.biostd.integration.SerializationConfig
import ac.uk.ebi.biostd.submission.submitter.SubmissionSubmitter
import ac.uk.ebi.biostd.submission.handlers.FilesCopier
import ac.uk.ebi.biostd.submission.handlers.FilesHandler
import ac.uk.ebi.biostd.submission.handlers.FilesValidator
import ac.uk.ebi.biostd.submission.handlers.OutputFilesGenerator
import ac.uk.ebi.biostd.submission.processors.AccNoProcessor
import ac.uk.ebi.biostd.submission.processors.AccessTagProcessor
import ac.uk.ebi.biostd.submission.processors.IProjectProcessor
import ac.uk.ebi.biostd.submission.processors.ProjectProcessor
import ac.uk.ebi.biostd.submission.processors.PropertiesProcessor
import ac.uk.ebi.biostd.submission.processors.SubmissionProcessor
import ac.uk.ebi.biostd.submission.processors.TimesProcessor
import ac.uk.ebi.biostd.submission.submitter.ProjectSubmitter
import ac.uk.ebi.biostd.submission.util.AccNoPatternUtil
import ac.uk.ebi.biostd.submission.validators.IProjectValidator
import ac.uk.ebi.biostd.submission.validators.ProjectValidator
import ac.uk.ebi.biostd.submission.validators.SubmissionProjectValidator
import ac.uk.ebi.biostd.submission.validators.SubmissionValidator
import ebi.ac.uk.paths.SubmissionFolderResolver
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Lazy
import java.nio.file.Paths

@Configuration
@Import(ValidatorConfig::class, ProcessorConfig::class, FilesHandlerConfig::class, SecurityBeansConfig::class)
class SubmitterConfig {
    @Bean
    fun submissionSubmitter(
        validators: List<SubmissionValidator>,
        processors: List<SubmissionProcessor>,
        filesHandler: FilesHandler
    ) = SubmissionSubmitter(validators, processors, filesHandler)

    @Bean
    fun projectSubmitter(
        accNoPatternUtil: AccNoPatternUtil,
        validators: List<IProjectValidator>,
        processors: List<IProjectProcessor>
    ) = ProjectSubmitter(accNoPatternUtil, validators, processors)

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
    class ProcessorConfig(private val userPrivilegesService: IUserPrivilegesService) {
        @Bean
        fun accNoProcessor() = AccNoProcessor(userPrivilegesService, accNoPatternUtil())

        @Bean
        fun accNoPatternUtil() = AccNoPatternUtil()

        @Bean
        fun accessTagProcessor() = AccessTagProcessor()

        @Bean
        fun timesProcessor() = TimesProcessor()

        @Bean
        fun propertiesProcessor() = PropertiesProcessor()

        @Bean
        fun projectProcessor() = ProjectProcessor()
    }

    @Configuration
    class ValidatorConfig(
        private val accNoPatternUtil: AccNoPatternUtil,
        private val userPrivilegesService: IUserPrivilegesService
    ) {
        @Bean
        fun submissionProjectValidator() = SubmissionProjectValidator()

        @Bean
        fun projectValidator() = ProjectValidator(accNoPatternUtil, userPrivilegesService)
    }
}
