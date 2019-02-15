package ac.uk.ebi.pmc.process

import ac.uk.ebi.biostd.SerializationService
import ac.uk.ebi.pmc.client.PmcApi
import ac.uk.ebi.pmc.persistence.MongoDocService
import ac.uk.ebi.pmc.persistence.SubmissionDocService
import ac.uk.ebi.scheduler.properties.PmcImporterProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy

@Configuration
@Lazy
class ProcessorConfig {

    @Bean
    fun fileDownloader(pmcApi: PmcApi, properties: PmcImporterProperties) = FileDownloader(properties, pmcApi)

    @Bean
    fun pmcProcessor(
        docService: MongoDocService,
        serializationService: SerializationService,
        submissionDocService: SubmissionDocService,
        fileDownloader: FileDownloader
    ) = PmcProcessor(docService, serializationService, submissionDocService, fileDownloader)

    @Bean
    fun pmcSubmissionProcessor(pmcImporter: PmcProcessor) = PmcSubmissionProcessor(pmcImporter)
}
