package ac.uk.ebi.pmc.load

import ac.uk.ebi.biostd.SerializationService
import ac.uk.ebi.pmc.persistence.MongoDocService
import ac.uk.ebi.pmc.persistence.SubmissionDocService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy

@Configuration
@Lazy
class LoaderConfig {

    @Bean
    fun pmcSubmissionLoader(
        docService: MongoDocService,
        submissionDocService: SubmissionDocService,
        serializationService: SerializationService
    ) = PmcSubmissionLoader(serializationService, docService, submissionDocService)

    @Bean
    fun pmcLoader(pmcSubmissionLoader: PmcSubmissionLoader) = PmcLoader(pmcSubmissionLoader)
}
