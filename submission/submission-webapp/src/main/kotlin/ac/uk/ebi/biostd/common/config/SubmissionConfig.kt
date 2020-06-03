package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.events.EventsService
import ac.uk.ebi.biostd.files.service.UserFilesService
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.persistence.integration.PersistenceContext
import ac.uk.ebi.biostd.persistence.integration.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.service.ProjectRepository
import ac.uk.ebi.biostd.persistence.service.SubmissionRepository
import ac.uk.ebi.biostd.submission.domain.helpers.SourceGenerator
import ac.uk.ebi.biostd.submission.domain.service.ExtSubmissionService
import ac.uk.ebi.biostd.submission.domain.service.ProjectService
import ac.uk.ebi.biostd.submission.domain.service.SubmissionService
import ac.uk.ebi.biostd.submission.submitter.SubmissionPersistenceService
import ac.uk.ebi.biostd.submission.submitter.SubmissionSubmitter
import ac.uk.ebi.biostd.submission.web.handlers.SubmissionsWebHandler
import ac.uk.ebi.biostd.submission.web.handlers.SubmitWebHandler
import ebi.ac.uk.security.integration.components.ISecurityService
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(value = [PersistenceConfig::class, SecurityBeansConfig::class])
class SubmissionConfig(
    private val sourceGenerator: SourceGenerator,
    private val serializationService: SerializationService
) {
    @Bean
    @Suppress("LongParameterList")
    fun submissionService(
        subRepository: SubmissionRepository,
        serializationService: SerializationService,
        userPrivilegeService: IUserPrivilegesService,
        queryService: SubmissionQueryService,
        submissionSubmitter: SubmissionSubmitter,
        eventsService: EventsService
    ): SubmissionService = SubmissionService(
        subRepository,
        serializationService,
        userPrivilegeService,
        queryService,
        submissionSubmitter,
        eventsService)

    @Bean
    fun extSubmissionService(
        persistenceContext: PersistenceContext,
        subRepository: SubmissionRepository,
        userPrivilegeService: IUserPrivilegesService
    ): ExtSubmissionService = ExtSubmissionService(persistenceContext, subRepository, userPrivilegeService)

    @Bean
    fun projectService(
        projectRepository: ProjectRepository,
        userPrivilegeService: IUserPrivilegesService
    ): ProjectService = ProjectService(projectRepository, userPrivilegeService)

    @Bean
    fun submitHandler(
        submissionService: SubmissionService,
        userFilesService: UserFilesService,
        securityService: ISecurityService
    ): SubmitWebHandler =
        SubmitWebHandler(
            submissionService,
            sourceGenerator,
            serializationService,
            userFilesService,
            securityService
        )

    @Bean
    fun submissionHandler(submissionService: SubmissionService): SubmissionsWebHandler =
        SubmissionsWebHandler(submissionService)

    @Bean
    fun persistenceService(submissionRepository: SubmissionRepository, persistenceContext: PersistenceContext):
        SubmissionPersistenceService = SubmissionPersistenceService(submissionRepository, persistenceContext)
}
