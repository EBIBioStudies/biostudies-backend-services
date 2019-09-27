package ac.uk.ebi.biostd.submission.domain.service

import ac.uk.ebi.biostd.persistence.model.AccessTag
import ac.uk.ebi.biostd.persistence.model.AccessType
import ac.uk.ebi.biostd.persistence.repositories.AccessPermissionRepository
import ac.uk.ebi.biostd.persistence.repositories.TagsDataRepository
import ac.uk.ebi.biostd.persistence.service.SubmissionRepository
import ac.uk.ebi.biostd.submission.submitter.ProjectSubmitter
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.Project
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.extensions.title
import ebi.ac.uk.persistence.PersistenceContext
import ebi.ac.uk.security.integration.model.api.SecurityUser

class ProjectService(
    private val submitter: ProjectSubmitter,
    private val persistenceContext: PersistenceContext,
    private val tagsDataRepository: TagsDataRepository,
    private val submissionRepository: SubmissionRepository,
    private val accessPermissionRepository: AccessPermissionRepository
) {
    fun submit(project: Submission, user: SecurityUser) =
        submitter.submit(ExtendedSubmission(project, user.asUser()), persistenceContext)

    fun getAllowedProjects(user: SecurityUser, accessType: AccessType): List<Project> {
        val accessTags = if (user.superuser) tagsDataRepository.findAll() else getUserTags(user, accessType)
        return submissionRepository.findProjectsByAccessTags(accessTags).map { Project(it.accNo, it.title) }
    }

    private fun getUserTags(user: SecurityUser, accessType: AccessType): List<AccessTag> =
        accessPermissionRepository.findByUserIdAndAccessType(user.id, accessType).map { it.accessTag }
}
