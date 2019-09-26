package ac.uk.ebi.biostd.submission.domain.service

import ac.uk.ebi.biostd.persistence.model.AccessTag
import ac.uk.ebi.biostd.persistence.model.AccessType
import ac.uk.ebi.biostd.persistence.repositories.AccessPermissionRepository
import ac.uk.ebi.biostd.persistence.repositories.TagsDataRepository
import ac.uk.ebi.biostd.persistence.service.SubmissionRepository
import ebi.ac.uk.model.Project
import ebi.ac.uk.model.extensions.title
import ebi.ac.uk.security.integration.model.api.SecurityUser

class ProjectService(
    private val submissionRepository: SubmissionRepository,
    private val accessPermissionRepository: AccessPermissionRepository,
    private val tagsDataRepository: TagsDataRepository
) {
    fun getAllowedProjects(user: SecurityUser, accessType: AccessType): List<Project> {
        val accessTags = if (user.superuser) tagsDataRepository.findAll() else getUserTags(user, accessType)
        return submissionRepository.findProjectsByAccessTags(accessTags).map { Project(it.accNo, it.title) }
    }

    private fun getUserTags(user: SecurityUser, accessType: AccessType): List<AccessTag> =
        accessPermissionRepository.findByUserIdAndAccessType(user.id, accessType).map { it.accessTag }
}
