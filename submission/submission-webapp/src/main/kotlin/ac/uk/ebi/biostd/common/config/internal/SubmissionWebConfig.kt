package ac.uk.ebi.biostd.common.config.internal

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.submission.domain.submission.SubmissionQueryService
import ac.uk.ebi.biostd.submission.domain.submission.SubmissionService
import ac.uk.ebi.biostd.submission.web.handlers.SubmissionsWebHandler
import ac.uk.ebi.biostd.submission.web.resources.ext.ExtendedPageMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.net.URI

@Configuration
class SubmissionWebConfig {

    @Bean
    fun submissionHandler(
        submissionService: SubmissionService,
        submissionQueryService: SubmissionQueryService,
    ): SubmissionsWebHandler = SubmissionsWebHandler(submissionService, submissionQueryService)

    @Bean
    fun extPageMapper(properties: ApplicationProperties): ExtendedPageMapper =
        ExtendedPageMapper(URI.create(properties.instanceBaseUrl))
}
