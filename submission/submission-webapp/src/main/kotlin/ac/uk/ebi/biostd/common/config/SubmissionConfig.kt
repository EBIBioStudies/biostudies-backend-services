package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.persistence.common.service.CollectionDataService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionDraftPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionFilesPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.api.FileStorageService
import ac.uk.ebi.biostd.stats.web.TempFileGenerator
import ac.uk.ebi.biostd.submission.domain.helpers.CollectionService
import ac.uk.ebi.biostd.submission.domain.helpers.OnBehalfUtils
import ac.uk.ebi.biostd.submission.domain.service.ExtSubmissionQueryService
import ac.uk.ebi.biostd.submission.domain.service.ExtSubmissionService
import ac.uk.ebi.biostd.submission.domain.service.SubmissionDraftService
import ac.uk.ebi.biostd.submission.domain.service.SubmissionQueryService
import ac.uk.ebi.biostd.submission.domain.service.SubmissionService
import ac.uk.ebi.biostd.submission.stats.SubmissionStatsService
import ac.uk.ebi.biostd.submission.submitter.ExtSubmissionSubmitter
import ac.uk.ebi.biostd.submission.submitter.SubmissionStagesHandler
import ac.uk.ebi.biostd.submission.submitter.SubmissionSubmitter
import ac.uk.ebi.biostd.submission.web.handlers.SubmitRequestBuilder
import ac.uk.ebi.biostd.submission.web.handlers.SubmitWebHandler
import ebi.ac.uk.extended.mapping.to.ToFileListMapper
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
import ebi.ac.uk.security.integration.components.ISecurityQueryService
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import uk.ac.ebi.events.service.EventsPublisherService

@Suppress("LongParameterList")
@Configuration
@Import(value = [FilePersistenceConfig::class, SecurityConfig::class])
class SubmissionConfig {
    @Bean
    fun submissionQueryService(
        submissionPersistenceQueryService: SubmissionPersistenceQueryService,
        filesRepository: SubmissionFilesPersistenceService,
        serializationService: SerializationService,
        toSubmissionMapper: ToSubmissionMapper,
        toFileListMapper: ToFileListMapper,
    ): SubmissionQueryService = SubmissionQueryService(
        submissionPersistenceQueryService,
        filesRepository,
        serializationService,
        toSubmissionMapper,
        toFileListMapper,
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
        submissionPersistenceService,
        fileStorageService
    )

    @Bean
    fun submissionStagesHandler(
        statsService: SubmissionStatsService,
        submissionSubmitter: SubmissionSubmitter,
        eventsPublisherService: EventsPublisherService,
    ): SubmissionStagesHandler = SubmissionStagesHandler(statsService, submissionSubmitter, eventsPublisherService)

    @Bean
    fun extSubmissionQueryService(
        queryService: SubmissionPersistenceQueryService,
        filesRepository: SubmissionFilesPersistenceService,
    ): ExtSubmissionQueryService = ExtSubmissionQueryService(filesRepository, queryService)

    @Bean
    fun extSubmissionService(
        submissionSubmitter: ExtSubmissionSubmitter,
        submissionPersistenceQueryService: SubmissionPersistenceQueryService,
        userPrivilegeService: IUserPrivilegesService,
        securityQueryService: ISecurityQueryService,
        eventsPublisherService: EventsPublisherService,
    ): ExtSubmissionService =
        ExtSubmissionService(
            submissionSubmitter,
            submissionPersistenceQueryService,
            userPrivilegeService,
            securityQueryService,
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
    fun submitRequestBuilder(
        tempFileGenerator: TempFileGenerator,
        onBehalfUtils: OnBehalfUtils,
    ): SubmitRequestBuilder = SubmitRequestBuilder(tempFileGenerator, onBehalfUtils)

    @Bean
    fun onBehalfUtils(securityQueryService: ISecurityQueryService): OnBehalfUtils = OnBehalfUtils(securityQueryService)
}
