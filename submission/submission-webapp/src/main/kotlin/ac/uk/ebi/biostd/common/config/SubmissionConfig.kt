package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.persistence.integration.PersistenceContext
import ac.uk.ebi.biostd.persistence.repositories.AccessPermissionRepository
import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepository
import ac.uk.ebi.biostd.persistence.service.ProjectRepository
import ac.uk.ebi.biostd.persistence.service.SubmissionRepository
import ac.uk.ebi.biostd.submission.domain.helpers.SourceGenerator
import ac.uk.ebi.biostd.submission.domain.service.ProjectService
import ac.uk.ebi.biostd.submission.domain.service.SubmissionService
import ac.uk.ebi.biostd.submission.submitter.ProjectSubmitter
import ac.uk.ebi.biostd.submission.submitter.SubmissionSubmitter
import ac.uk.ebi.biostd.submission.web.handlers.ProjectWebHandler
import ac.uk.ebi.biostd.submission.web.handlers.SubmissionWebHandler
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
    fun submissionService(
        subRepository: SubmissionRepository,
        serializationService: SerializationService,
        persistenceContext: PersistenceContext,
        userPrivilegeService: IUserPrivilegesService,
        submissionSubmitter: SubmissionSubmitter
    ): SubmissionService = SubmissionService(
        subRepository, persistenceContext, serializationService, userPrivilegeService, submissionSubmitter)

    @Bean
    fun projectService(
        projectSubmitter: ProjectSubmitter,
        tagsDataRepository: AccessTagDataRepository,
        projectRepository: ProjectRepository,
        accessPermissionRepository: AccessPermissionRepository
    ): ProjectService = ProjectService(
        projectSubmitter, tagsDataRepository, projectRepository, accessPermissionRepository)

    @Bean
    fun submissionHandler(submissionService: SubmissionService): SubmissionWebHandler =
        SubmissionWebHandler(submissionService, sourceGenerator, serializationService)

    @Bean
    fun projectHandler(projectService: ProjectService) =
        ProjectWebHandler(projectService, serializationService)
}
