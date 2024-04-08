package ac.uk.ebi.pmc.process

import ac.uk.ebi.pmc.persistence.ErrorsDocService
import ac.uk.ebi.pmc.persistence.SubmissionDocService
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
        errorsDocService: ErrorsDocService,
        submissionInitializer: SubmissionInitializer,
        submissionDocService: SubmissionDocService,
        fileDownloader: FileDownloader,
    ) = PmcProcessor(errorsDocService, submissionInitializer, submissionDocService, fileDownloader)
}
