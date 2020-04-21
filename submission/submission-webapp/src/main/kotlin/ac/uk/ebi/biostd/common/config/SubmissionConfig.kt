package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.persistence.integration.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.service.ProjectRepository
import ac.uk.ebi.biostd.persistence.service.SubmissionRepository
import ac.uk.ebi.biostd.submission.domain.helpers.SourceGenerator
import ac.uk.ebi.biostd.submission.domain.service.ProjectService
import ac.uk.ebi.biostd.submission.domain.service.SubmissionService
import ac.uk.ebi.biostd.submission.submitter.SubmissionSubmitter
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
        userPrivilegeService: IUserPrivilegesService,
        queryService: SubmissionQueryService,
        submissionSubmitter: SubmissionSubmitter
    ): SubmissionService = SubmissionService(
        subRepository, serializationService, userPrivilegeService, queryService, submissionSubmitter)

    @Bean
    fun projectService(
        projectRepository: ProjectRepository,
        userPrivilegeService: IUserPrivilegesService
    ): ProjectService = ProjectService(projectRepository, userPrivilegeService)

    @Bean
    fun submissionHandler(submissionService: SubmissionService): SubmissionWebHandler =
        SubmissionWebHandler(submissionService, sourceGenerator, serializationService)
}
