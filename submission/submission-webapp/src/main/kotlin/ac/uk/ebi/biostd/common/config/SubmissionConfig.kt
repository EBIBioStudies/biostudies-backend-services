package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.files.service.UserFilesService
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.persistence.common.service.CollectionDataService
import ac.uk.ebi.biostd.persistence.common.service.StatsDataService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionDraftPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionFilesPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.api.FileStorageService
import ac.uk.ebi.biostd.stats.domain.service.SubmissionStatsService
import ac.uk.ebi.biostd.stats.web.handlers.StatsFileHandler
import ac.uk.ebi.biostd.submission.domain.helpers.CollectionService
import ac.uk.ebi.biostd.submission.domain.helpers.OnBehalfUtils
import ac.uk.ebi.biostd.submission.domain.helpers.TempFileGenerator
import ac.uk.ebi.biostd.submission.domain.service.ExtSubmissionQueryService
import ac.uk.ebi.biostd.submission.domain.service.ExtSubmissionService
import ac.uk.ebi.biostd.submission.domain.service.SubmissionDraftService
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
@Suppress("LongParameterList", "TooManyFunctions")
@Import(value = [FilePersistenceConfig::class, SecurityBeansConfig::class])
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
        submissionPersistenceService: SubmissionPersistenceService,
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
        fileStorageService,
        submissionPersistenceService,
    )

    @Bean
    fun submissionStatsService(
        statsFileHandler: StatsFileHandler,
        tempFileGenerator: TempFileGenerator,
        submissionStatsService: StatsDataService
    ): SubmissionStatsService = SubmissionStatsService(statsFileHandler, tempFileGenerator, submissionStatsService)

    @Bean
    fun extSubmissionQueryService(
        filesService: SubmissionFilesPersistenceService,
        requestService: SubmissionRequestPersistenceService,
        queryService: SubmissionPersistenceQueryService,
    ): ExtSubmissionQueryService = ExtSubmissionQueryService(requestService, filesService, queryService)

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
    fun submissionDraftService(
        submitWebHandler: SubmitWebHandler,
        toSubmissionMapper: ToSubmissionMapper,
        serializationService: SerializationService,
        submitRequestBuilder: SubmitRequestBuilder,
        userPrivilegesService: IUserPrivilegesService,
        submissionQueryService: SubmissionPersistenceQueryService,
        persistenceDraftService: SubmissionDraftPersistenceService,
    ): SubmissionDraftService =
        SubmissionDraftService(
            submitWebHandler,
            toSubmissionMapper,
            serializationService,
            submitRequestBuilder,
            userPrivilegesService,
            submissionQueryService,
            persistenceDraftService,
        )

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
