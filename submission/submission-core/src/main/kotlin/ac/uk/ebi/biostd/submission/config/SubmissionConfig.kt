package ac.uk.ebi.biostd.submission.config

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionFilesPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.api.FileStorageService
import ac.uk.ebi.biostd.submission.domain.extended.ExtSubmissionQueryService
import ac.uk.ebi.biostd.submission.domain.extended.ExtSubmissionService
import ac.uk.ebi.biostd.submission.domain.extended.ExtSubmissionSubmitter
import ac.uk.ebi.biostd.submission.domain.request.SubmissionStagesHandler
import ac.uk.ebi.biostd.submission.domain.submission.SubmissionQueryService
import ac.uk.ebi.biostd.submission.domain.submission.SubmissionService
import ac.uk.ebi.biostd.submission.domain.submission.SubmissionSubmitter
import ac.uk.ebi.biostd.submission.stats.SubmissionStatsService
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
        extSubmissionSubmitter: ExtSubmissionSubmitter,
        eventsPublisherService: EventsPublisherService,
    ): SubmissionStagesHandler = SubmissionStagesHandler(statsService, extSubmissionSubmitter, eventsPublisherService)

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
}