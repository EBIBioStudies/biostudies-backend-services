package ac.uk.ebi.biostd.config

import ac.uk.ebi.biostd.SerializationService
import ac.uk.ebi.biostd.config.SubmitterConfig.ProcessorConfig
import ac.uk.ebi.biostd.config.SubmitterConfig.FilesProcessorConfig
import ac.uk.ebi.biostd.property.ApplicationProperties
import ac.uk.ebi.biostd.submission.SubmissionSubmitter
import ac.uk.ebi.biostd.submission.processors.AccNoProcessor
import ac.uk.ebi.biostd.submission.processors.AccessTagProcessor
import ac.uk.ebi.biostd.submission.processors.TimesProcessor
import ac.uk.ebi.biostd.submission.processors.FilesProcessor
import ac.uk.ebi.biostd.submission.processors.SubmissionProcessor
import ebi.ac.uk.paths.FolderResolver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(ProcessorConfig::class, FilesProcessorConfig::class)
class SubmitterConfig {

    @Bean
    fun submissionSubmitter(processors: List<SubmissionProcessor>) = SubmissionSubmitter(processors)

    @Configuration
    class FilesProcessorConfig(private val appProperties: ApplicationProperties) {

        @Bean
        fun folderResolver() = FolderResolver(appProperties.basePath)

        @Bean
        fun serializationService() = SerializationService()

        @Bean
        fun filesProcessor() = FilesProcessor(folderResolver(), serializationService())
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
}