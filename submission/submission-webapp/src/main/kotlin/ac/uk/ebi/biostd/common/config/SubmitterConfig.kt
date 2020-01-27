package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.common.config.SubmitterConfig.FilesHandlerConfig
import ac.uk.ebi.biostd.common.config.SubmitterConfig.ProcessorConfig
import ac.uk.ebi.biostd.common.property.ApplicationProperties
import ac.uk.ebi.biostd.integration.SerializationConfig
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
import ac.uk.ebi.biostd.submission.processors.SubmissionProjectProcessor
import ac.uk.ebi.biostd.submission.processors.TimesProcessor
import ac.uk.ebi.biostd.submission.submitter.ProjectSubmitter
import ac.uk.ebi.biostd.submission.submitter.SubmissionSubmitter
import ac.uk.ebi.biostd.submission.util.AccNoPatternUtil
import ebi.ac.uk.paths.SubmissionFolderResolver
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Lazy
import org.springframework.core.annotation.Order
import java.nio.file.Paths

@Configuration
@Import(ProcessorConfig::class, FilesHandlerConfig::class, SecurityBeansConfig::class)
class SubmitterConfig {
    @Bean
    fun submissionSubmitter(
        processors: List<SubmissionProcessor>,
        filesHandler: FilesHandler
    ) = SubmissionSubmitter(processors, filesHandler)

    @Bean
    fun projectSubmitter(
        accNoPatternUtil: AccNoPatternUtil,
        processors: List<IProjectProcessor>
    ) = ProjectSubmitter(accNoPatternUtil, processors)

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
    class ProcessorConfig(private val userPrivilegesService: IUserPrivilegesService) {
        @Bean
        @Order(0)
        fun submissionProjectProcessor() = SubmissionProjectProcessor()

        @Bean
        @Order(1)
        fun accessTagProcessor() = AccessTagProcessor()

        @Bean
        @Order(2)
        fun accNoProcessor() = AccNoProcessor(userPrivilegesService, accNoPatternUtil())

        @Bean
        @Order(3)
        fun timesProcessor() = TimesProcessor()

        @Bean
        @Order(4)
        fun propertiesProcessor() = PropertiesProcessor()

        @Bean
        fun accNoPatternUtil() = AccNoPatternUtil()

        @Bean
        fun projectProcessor() = ProjectProcessor(accNoPatternUtil(), userPrivilegesService)
    }
}
