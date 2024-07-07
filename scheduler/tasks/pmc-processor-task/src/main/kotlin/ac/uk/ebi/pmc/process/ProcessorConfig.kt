package ac.uk.ebi.pmc.process

import ac.uk.ebi.pmc.persistence.domain.ErrorsService
import ac.uk.ebi.pmc.persistence.domain.SubmissionService
import ac.uk.ebi.pmc.process.util.FileDownloader
import ac.uk.ebi.pmc.process.util.SubmissionInitializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy

@Configuration
@Lazy
class ProcessorConfig {
    @Bean
    fun pmcProcessor(
        errorsDocService: ErrorsService,
        submissionInitializer: SubmissionInitializer,
        submissionDocService: SubmissionService,
        fileDownloader: FileDownloader,
    ) = PmcProcessor(submissionInitializer, errorsDocService, submissionDocService, fileDownloader)
}
