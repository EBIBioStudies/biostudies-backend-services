package ac.uk.ebi.biostd.submission.domain.service

import ac.uk.ebi.biostd.persistence.model.AccessTag
import ac.uk.ebi.biostd.persistence.model.AccessType
import ac.uk.ebi.biostd.persistence.repositories.AccessPermissionRepository
import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepo
import ac.uk.ebi.biostd.persistence.service.ProjectRepository
import ebi.ac.uk.model.Project
import ebi.ac.uk.security.integration.model.api.SecurityUser

class ProjectService(
    private val tagsDataRepository: AccessTagDataRepo,
    private val projectRepository: ProjectRepository,
    private val accessPermissionRepository: AccessPermissionRepository
) {
    fun getAllowedProjects(user: SecurityUser, accessType: AccessType): List<Project> {
        val accessTags = if (user.superuser) tagsDataRepository.findAll() else getUserTags(user, accessType)
        return projectRepository.findProjectsByAccessTags(accessTags).map { Project(it.accNo, it.title) }
    }

    private fun getUserTags(user: SecurityUser, accessType: AccessType): List<AccessTag> =
        accessPermissionRepository.findByUserIdAndAccessType(user.id, accessType).map { it.accessTag }
}
