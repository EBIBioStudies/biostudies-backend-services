package ac.uk.ebi.pmc.submit

import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.pmc.persistence.ErrorsDocService
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
            .create(requireNotNull(properties.bioStudiesUrl) { "biostudies system url is required" })
            .getAuthenticatedClient(
                requireNotNull(properties.bioStudiesUser) { "biostudies user name need to be configured" },
                requireNotNull(properties.bioStudiesPassword) { "biostudies user password name need to be configured" })

    @Bean
    fun pmcSubmitter(
        bioWebClient: BioWebClient,
        errorsDocService: ErrorsDocService,
        submissionDocService: SubmissionDocService
    ) = PmcSubmitter(bioWebClient, errorsDocService, submissionDocService)

    @Bean
    fun pmcSubmissionSubmitter(pmcSubmitter: PmcSubmitter) = PmcSubmissionSubmitter(pmcSubmitter)
}
