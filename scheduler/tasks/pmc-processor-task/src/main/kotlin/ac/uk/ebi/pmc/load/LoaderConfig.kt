package ac.uk.ebi.pmc.load

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.pmc.persistence.ErrorsDocService
import ac.uk.ebi.pmc.persistence.InputFilesDocService
import ac.uk.ebi.pmc.persistence.SubmissionDocService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy

@Configuration
@Lazy
class LoaderConfig {
    @Bean
    fun pmcSubmissionLoader(
        inputFilesDocService: InputFilesDocService,
        errorsDocService: ErrorsDocService,
        submissionDocService: SubmissionDocService,
        pmcSubmissionTabProcessor: PmcSubmissionTabProcessor,
    ) = PmcSubmissionLoader(pmcSubmissionTabProcessor, errorsDocService, inputFilesDocService, submissionDocService)

    @Bean
    fun pmcLoader(pmcSubmissionLoader: PmcSubmissionLoader) = PmcFileLoader(pmcSubmissionLoader)

    @Bean
    fun pmcSubmissionTabProcessor(serializationService: SerializationService) = PmcSubmissionTabProcessor(serializationService)
}
