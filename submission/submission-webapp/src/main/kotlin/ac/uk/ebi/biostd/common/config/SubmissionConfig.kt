package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.persistence.repositories.AccessPermissionRepository
import ac.uk.ebi.biostd.persistence.repositories.TagsDataRepository
import ac.uk.ebi.biostd.persistence.service.SubmissionRepository
import ac.uk.ebi.biostd.submission.SubmissionSubmitter
import ac.uk.ebi.biostd.submission.domain.service.ProjectService
import ac.uk.ebi.biostd.submission.domain.service.SubmissionService
import ac.uk.ebi.biostd.submission.domain.service.TempFileGenerator
import ac.uk.ebi.biostd.submission.submitter.ProjectSubmitter
import ac.uk.ebi.biostd.submission.web.handlers.PageTabReader
import ac.uk.ebi.biostd.submission.web.handlers.ProjectWebHandler
import ac.uk.ebi.biostd.submission.web.handlers.SubmissionWebHandler
import ebi.ac.uk.persistence.PersistenceContext
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(value = [PersistenceConfig::class, SecurityBeansConfig::class])
class SubmissionConfig(
    private val pageTabReader: PageTabReader,
    private val tmpFileGenerator: TempFileGenerator,
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
        persistenceContext: PersistenceContext,
        tagsDataRepository: TagsDataRepository,
        submissionRepository: SubmissionRepository,
        accessPermissionRepository: AccessPermissionRepository
    ): ProjectService = ProjectService(
        projectSubmitter, persistenceContext, tagsDataRepository, submissionRepository, accessPermissionRepository)

    @Bean
    fun submissionHandler(submissionService: SubmissionService): SubmissionWebHandler =
        SubmissionWebHandler(pageTabReader, submissionService, tmpFileGenerator, serializationService)

    @Bean
    fun projectHandler(projectService: ProjectService) =
        ProjectWebHandler(pageTabReader, projectService, tmpFileGenerator, serializationService)
}
