package ac.uk.ebi.biostd.common.config.internal

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.files.service.FileServiceFactory
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionMetaQueryService
import ac.uk.ebi.biostd.submission.domain.service.ExtSubmissionQueryService
import ac.uk.ebi.biostd.submission.domain.service.SubmissionQueryService
import ac.uk.ebi.biostd.submission.domain.service.SubmissionService
import ac.uk.ebi.biostd.submission.service.FileSourcesService
import ac.uk.ebi.biostd.submission.web.handlers.SubmissionsWebHandler
import ac.uk.ebi.biostd.submission.web.handlers.SubmitWebHandler
import ac.uk.ebi.biostd.submission.web.resources.ext.ExtendedPageMapper
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
import org.springframework.context.annotation.Bean
import java.net.URI

class SubmissionWebConfig(
    private val fileSourcesService: FileSourcesService,
    private val serializationService: SerializationService,
) {

    @Bean
    fun submitHandler(
        submissionService: SubmissionService,
        extSubmissionQueryService: ExtSubmissionQueryService,
        toSubmissionMapper: ToSubmissionMapper,
        queryService: SubmissionMetaQueryService,
        fileServiceFactory: FileServiceFactory,
    ): SubmitWebHandler =
        SubmitWebHandler(
            submissionService,
            extSubmissionQueryService,
            fileSourcesService,
            serializationService,
            toSubmissionMapper,
            queryService,
            fileServiceFactory
        )

    @Bean
    fun submissionHandler(
        submissionService: SubmissionService,
        submissionQueryService: SubmissionQueryService,
    ): SubmissionsWebHandler = SubmissionsWebHandler(submissionService, submissionQueryService)

    @Bean
    fun extPageMapper(properties: ApplicationProperties): ExtendedPageMapper =
        ExtendedPageMapper(URI.create(properties.instanceBaseUrl))
}
