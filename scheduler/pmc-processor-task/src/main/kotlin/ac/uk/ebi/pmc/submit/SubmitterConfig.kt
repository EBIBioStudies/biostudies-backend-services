package ac.uk.ebi.pmc.submit

import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.pmc.persistence.MongoDocService
import ac.uk.ebi.pmc.persistence.SubmissionDocService
import ac.uk.ebi.scheduler.properties.PmcImporterProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy

@Configuration
@Lazy
class SubmitterConfig {

    @Bean
    fun bioWebClient(properties: PmcImporterProperties) =
        SecurityWebClient
            .create(properties.bioStudiesUrl)
            .getAuthenticatedClient(properties.bioStudiesUser, properties.bioStudiesPassword)

    @Bean
    fun pmcSubmitter(
        bioWebClient: BioWebClient,
        docService: MongoDocService,
        submissionDocService: SubmissionDocService
    ) = PmcSubmitter(bioWebClient, docService, submissionDocService)

    @Bean
    fun pmSubmissionSubmitter(pmcSubmitter: PmcSubmitter) = PmcSubmissionSubmitter(pmcSubmitter)
}
