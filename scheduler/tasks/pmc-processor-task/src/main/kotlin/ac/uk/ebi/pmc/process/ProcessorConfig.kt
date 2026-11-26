package ac.uk.ebi.pmc.process

import ac.uk.ebi.pmc.persistence.domain.ErrorsService
import ac.uk.ebi.pmc.persistence.domain.SubmissionService
import ac.uk.ebi.pmc.process.util.FileDownloader
import ac.uk.ebi.scheduler.properties.PmcImporterProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import uk.ac.ebi.biostd.integration.SerializationService

@Configuration
@Lazy
class ProcessorConfig {
    @Bean
    fun pmcProcessor(
        errorsDocService: ErrorsService,
        serializationService: SerializationService,
        submissionDocService: SubmissionService,
        fileDownloader: FileDownloader,
        properties: PmcImporterProperties,
    ): PmcProcessor = PmcProcessor(serializationService, errorsDocService, submissionDocService, fileDownloader, properties)
}
