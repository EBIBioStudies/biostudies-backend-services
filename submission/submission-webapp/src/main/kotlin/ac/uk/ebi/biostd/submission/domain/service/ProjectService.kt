package ac.uk.ebi.biostd.submission.domain.service

import ac.uk.ebi.biostd.persistence.common.model.AccessType
import ac.uk.ebi.biostd.persistence.repositories.data.ProjectRepository
import ebi.ac.uk.model.Project
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import ebi.ac.uk.security.integration.model.api.SecurityUser

class ProjectService(
    private val projectRepository: ProjectRepository,
    private val userPrivilegesService: IUserPrivilegesService
) {
    fun getAllowedProjects(user: SecurityUser, accessType: AccessType): List<Project> {
        val accessTags = userPrivilegesService.allowedProjects(user.email, accessType)
        return projectRepository.findProjectsByAccessTags(accessTags).map { Project(it.accNo, it.title) }
    }
}
