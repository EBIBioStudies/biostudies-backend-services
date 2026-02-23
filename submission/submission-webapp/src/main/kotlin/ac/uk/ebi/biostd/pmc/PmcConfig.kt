package ac.uk.ebi.biostd.pmc

import ac.uk.ebi.biostd.submission.domain.extended.ExtSubmissionQueryService
import ac.uk.ebi.biostd.submission.domain.extended.ExtSubmissionService
import ebi.ac.uk.client.pmc.PmcClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.ac.ebi.extended.serialization.service.ExtSerializationService

@Configuration
class PmcConfig {
    @Bean
    fun pmcLinksLoader(
        queryService: ExtSubmissionQueryService,
        serializationService: ExtSerializationService,
        submissionService: ExtSubmissionService,
    ): PmcLinksLoader {
        return PmcLinksLoader(pmcWebClient(), queryService, serializationService, submissionService)
    }

    fun pmcWebClient(): PmcClient {
        return PmcClient.createClient("Ymlvc3R1ZGllczI6dGVzdEJpb3N0dWRpZXM=")
    }
}
