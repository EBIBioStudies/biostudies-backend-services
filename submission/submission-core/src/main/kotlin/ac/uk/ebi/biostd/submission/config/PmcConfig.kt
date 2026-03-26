package ac.uk.ebi.biostd.submission.config

import ac.uk.ebi.biostd.persistence.doc.service.DistributedLockService
import ac.uk.ebi.biostd.persistence.doc.service.PmcSubmissionQueryService
import ac.uk.ebi.biostd.submission.domain.extended.ExtSubmissionQueryService
import ac.uk.ebi.biostd.submission.domain.extended.ExtSubmissionService
import ac.uk.ebi.biostd.submission.domain.submitter.RemoteSubmitterExecutor
import ac.uk.ebi.biostd.submission.pmc.PmcLinksLoader
import ac.uk.ebi.biostd.submission.pmc.PmcLinksProcessor
import ac.uk.ebi.biostd.submission.pmc.PmcRemoteLinksLoader
import ebi.ac.uk.client.pmc.PmcClient
import ebi.ac.uk.security.integration.components.SecurityQueryService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import uk.ac.ebi.extended.serialization.service.ExtSerializationService

@EnableScheduling
@Configuration
class PmcConfig {
    @Bean
    fun pmcLinksLoader(
        queryService: ExtSubmissionQueryService,
        serializationService: ExtSerializationService,
    ): PmcLinksLoader =
        PmcLinksLoader(
            pmcWebClient(),
            queryService,
            serializationService,
        )

    @Bean
    fun pmcLinksProcessor(
        pmcSubmissionsService: PmcSubmissionQueryService,
        submissionService: ExtSubmissionService,
        securityQueryService: SecurityQueryService,
        pmcLinksLoader: PmcLinksLoader,
        distributedLockService: DistributedLockService,
    ): PmcLinksProcessor =
        PmcLinksProcessor(
            pmcSubmissionsService,
            submissionService,
            securityQueryService,
            pmcLinksLoader,
            distributedLockService,
        )

    @Bean
    fun pmcRemoteLinksLoader(remoteSubmitterExecutor: RemoteSubmitterExecutor): PmcRemoteLinksLoader =
        PmcRemoteLinksLoader(remoteSubmitterExecutor)

    fun pmcWebClient(): PmcClient = PmcClient.createClient("Ymlvc3R1ZGllczI6dGVzdEJpb3N0dWRpZXM=")
}
