package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.SerializationService
import ac.uk.ebi.biostd.common.config.SubmitterConfig.FilesHandlerConfig
import ac.uk.ebi.biostd.common.config.SubmitterConfig.ProcessorConfig
import ac.uk.ebi.biostd.common.config.SubmitterConfig.ValidatorConfig
import ac.uk.ebi.biostd.common.property.ApplicationProperties
import ac.uk.ebi.biostd.submission.SubmissionSubmitter
import ac.uk.ebi.biostd.submission.handlers.FilesHandler
import ac.uk.ebi.biostd.submission.processors.AccNoProcessor
import ac.uk.ebi.biostd.submission.processors.AccessTagProcessor
import ac.uk.ebi.biostd.submission.processors.SubmissionProcessor
import ac.uk.ebi.biostd.submission.processors.TimesProcessor
import ac.uk.ebi.biostd.submission.validators.ProjectValidator
import ac.uk.ebi.biostd.submission.validators.SubmissionValidator
import ebi.ac.uk.paths.FolderResolver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Lazy
import java.nio.file.Paths

@Configuration
@Import(ValidatorConfig::class, ProcessorConfig::class, FilesHandlerConfig::class)
class SubmitterConfig {

    @Bean
    fun submissionSubmitter(
        validators: List<SubmissionValidator>,
        processors: List<SubmissionProcessor>,
        filesHandler: FilesHandler
    ) = SubmissionSubmitter(validators, processors, filesHandler)

    @Configuration
    class FilesHandlerConfig(private val appProperties: ApplicationProperties) {

        @Bean
        @Lazy
        fun folderResolver() = FolderResolver(Paths.get(appProperties.basepath))

        @Bean
        fun serializationService() = SerializationService()

        @Bean
        fun filesHandler() = FilesHandler(folderResolver(), serializationService())
    }

    @Configuration
    class ProcessorConfig {
        @Bean
        fun accNoProcessor() = AccNoProcessor()

        @Bean
        fun accessTagProcessor() = AccessTagProcessor()

        @Bean
        fun timesProcessor() = TimesProcessor()
    }

    @Configuration
    class ValidatorConfig {
        @Bean
        fun projectValidator() = ProjectValidator()
    }
}
