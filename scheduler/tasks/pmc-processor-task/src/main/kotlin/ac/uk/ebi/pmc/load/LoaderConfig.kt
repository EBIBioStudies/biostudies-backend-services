package ac.uk.ebi.pmc.load

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.pmc.persistence.domain.ErrorsService
import ac.uk.ebi.pmc.persistence.domain.InputFilesService
import ac.uk.ebi.pmc.persistence.domain.SubmissionService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy

@Configuration
@Lazy
class LoaderConfig {
    @Bean
    fun pmcSubmissionLoader(
        inputFilesService: InputFilesService,
        errorsService: ErrorsService,
        submissionService: SubmissionService,
        pmcSubmissionTabProcessor: PmcSubmissionTabProcessor,
    ): PmcSubmissionLoader = PmcSubmissionLoader(pmcSubmissionTabProcessor, errorsService, inputFilesService, submissionService)

    @Bean
    fun pmcLoader(pmcSubmissionLoader: PmcSubmissionLoader) = PmcFileLoader(pmcSubmissionLoader)

    @Bean
    fun pmcSubmissionTabProcessor(serializationService: SerializationService) = PmcSubmissionTabProcessor(serializationService)
}
