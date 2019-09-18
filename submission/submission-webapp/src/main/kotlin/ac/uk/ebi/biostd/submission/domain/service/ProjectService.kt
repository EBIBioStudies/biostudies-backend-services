package ac.uk.ebi.biostd.submission.domain.service

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
        var accessTags = if (user.superuser) tagsDataRepository.findAll() else
            accessPermissionRepository.findByUserIdAndAccessType(user.id, accessType).map { it.accessTag }
        return submissionRepository.findSubmissionsByAccessTags(accessTags).map { submission ->
            Project(submission.accNo, submission.title)
        }
    }
}
