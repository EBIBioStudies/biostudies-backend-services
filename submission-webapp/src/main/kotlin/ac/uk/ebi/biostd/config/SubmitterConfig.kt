package ac.uk.ebi.biostd.config

import ac.uk.ebi.biostd.SerializationService
import ac.uk.ebi.biostd.config.SubmitterConfig.ProcessorConfig
import ac.uk.ebi.biostd.config.SubmitterConfig.SubFileManagerConfig
import ac.uk.ebi.biostd.property.ApplicationProperties
import ac.uk.ebi.biostd.submission.SubmissionSubmitter
import ac.uk.ebi.biostd.submission.helpers.AccNoProcessor
import ac.uk.ebi.biostd.submission.helpers.RelPathProcessor
import ac.uk.ebi.biostd.submission.helpers.TimesProcessor
import ac.uk.ebi.biostd.submission.procesing.SubFileManager
import ac.uk.ebi.biostd.submission.procesing.SubmissionProcessor
import ebi.ac.uk.paths.FolderResolver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(ProcessorConfig::class, SubFileManagerConfig::class)
class SubmitterConfig {

    @Bean
    fun submissionSubmitter(subProcessor: SubmissionProcessor, subFileManager: SubFileManager) =
            SubmissionSubmitter(subProcessor, subFileManager)

    @Configuration
    class SubFileManagerConfig(private val appProperties: ApplicationProperties) {

        @Bean
        fun folderResolver() = FolderResolver(appProperties.basePath)

        @Bean
        fun serializationService(): SerializationService = SerializationService()

        @Bean
        fun subFileManager(): SubFileManager = SubFileManager(folderResolver(), serializationService());
    }

    @Configuration
    class ProcessorConfig {

        @Bean
        fun submissionProcessor() = SubmissionProcessor(accNoProcessor(), relPathProcessor(), timesProcessor())

        @Bean
        fun accNoProcessor() = AccNoProcessor()

        @Bean
        fun relPathProcessor() = RelPathProcessor()

        @Bean
        fun timesProcessor() = TimesProcessor()
    }
}