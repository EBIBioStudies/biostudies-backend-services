package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.files.service.UserFilesService
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.persistence.common.service.CollectionDataService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.filesystem.api.FileStorageService
import ac.uk.ebi.biostd.submission.domain.helpers.CollectionService
import ac.uk.ebi.biostd.submission.domain.helpers.OnBehalfUtils
import ac.uk.ebi.biostd.submission.domain.helpers.TempFileGenerator
import ac.uk.ebi.biostd.submission.domain.service.ExtSubmissionQueryService
import ac.uk.ebi.biostd.submission.domain.service.ExtSubmissionService
import ac.uk.ebi.biostd.submission.domain.service.SubmissionQueryService
import ac.uk.ebi.biostd.submission.domain.service.SubmissionService
import ac.uk.ebi.biostd.submission.service.FileSourcesService
import ac.uk.ebi.biostd.submission.submitter.ExtSubmissionSubmitter
import ac.uk.ebi.biostd.submission.submitter.SubmissionSubmitter
import ac.uk.ebi.biostd.submission.web.handlers.SubmissionsWebHandler
import ac.uk.ebi.biostd.submission.web.handlers.SubmitRequestBuilder
import ac.uk.ebi.biostd.submission.web.handlers.SubmitWebHandler
import ac.uk.ebi.biostd.submission.web.resources.ext.ExtendedPageMapper
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
import ebi.ac.uk.security.integration.components.ISecurityQueryService
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import uk.ac.ebi.events.service.EventsPublisherService
import java.net.URI

@Configuration
@Suppress("LongParameterList")
@Import(value = [PersistenceConfig::class, SecurityBeansConfig::class])
class SubmissionConfig(
    private val fileSourcesService: FileSourcesService,
    private val serializationService: SerializationService,
) {

    @Bean
    fun submissionQueryService(
        submissionPersistenceQueryService: SubmissionPersistenceQueryService,
        serializationService: SerializationService,
        toSubmissionMapper: ToSubmissionMapper,
    ): SubmissionQueryService = SubmissionQueryService(
        submissionPersistenceQueryService, serializationService, toSubmissionMapper
    )

    @Bean
    fun submissionService(
        submissionPersistenceQueryService: SubmissionPersistenceQueryService,
        userPrivilegeService: IUserPrivilegesService,
        extSubmissionSubmitter: ExtSubmissionSubmitter,
        submissionSubmitter: SubmissionSubmitter,
        eventsPublisherService: EventsPublisherService,
        fileStorageService: FileStorageService,
    ): SubmissionService = SubmissionService(
        submissionPersistenceQueryService,
        userPrivilegeService,
        extSubmissionSubmitter,
        submissionSubmitter,
        eventsPublisherService,
        fileStorageService
    )

    @Bean
    fun extSubmissionQueryService(
        submissionPersistenceQueryService: SubmissionPersistenceQueryService,
    ): ExtSubmissionQueryService = ExtSubmissionQueryService(submissionPersistenceQueryService)

    @Bean
    fun extSubmissionService(
        submissionSubmitter: ExtSubmissionSubmitter,
        submissionPersistenceQueryService: SubmissionPersistenceQueryService,
        userPrivilegeService: IUserPrivilegesService,
        securityQueryService: ISecurityQueryService,
        properties: ApplicationProperties,
        eventsPublisherService: EventsPublisherService,
    ): ExtSubmissionService =
        ExtSubmissionService(
            submissionSubmitter,
            submissionPersistenceQueryService,
            userPrivilegeService,
            securityQueryService,
            properties,
            eventsPublisherService,
        )

    @Bean
    fun projectService(
        collectionSqlDataService: CollectionDataService,
        userPrivilegeService: IUserPrivilegesService,
    ): CollectionService = CollectionService(collectionSqlDataService, userPrivilegeService)

    @Bean
    fun submitHandler(
        submissionService: SubmissionService,
        userFilesService: UserFilesService,
        extSubmissionQueryService: ExtSubmissionQueryService,
        toSubmissionMapper: ToSubmissionMapper,
    ): SubmitWebHandler =
        SubmitWebHandler(
            submissionService,
            extSubmissionQueryService,
            fileSourcesService,
            serializationService,
            userFilesService,
            toSubmissionMapper,
        )

    @Bean
    fun submitRequestBuilder(
        tempFileGenerator: TempFileGenerator,
        onBehalfUtils: OnBehalfUtils,
    ): SubmitRequestBuilder = SubmitRequestBuilder(tempFileGenerator, onBehalfUtils)

    @Bean
    fun submissionHandler(
        submissionService: SubmissionService,
        submissionQueryService: SubmissionQueryService,
    ): SubmissionsWebHandler = SubmissionsWebHandler(submissionService, submissionQueryService)

    @Bean
    fun extPageMapper(properties: ApplicationProperties) = ExtendedPageMapper(URI.create(properties.instanceBaseUrl))

    @Bean
    fun onBehalfUtils(securityQueryService: ISecurityQueryService): OnBehalfUtils = OnBehalfUtils(securityQueryService)
}
