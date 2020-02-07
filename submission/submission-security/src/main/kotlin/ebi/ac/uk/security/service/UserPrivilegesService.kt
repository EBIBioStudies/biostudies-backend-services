package ebi.ac.uk.security.service

import ac.uk.ebi.biostd.persistence.model.AccessType
import ac.uk.ebi.biostd.persistence.repositories.AccessPermissionRepository
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import ebi.ac.uk.security.integration.exception.UserNotFoundByEmailException

internal class UserPrivilegesService(
    private val userRepository: UserDataRepository,
    private val accessPermissionRepository: AccessPermissionRepository
) : IUserPrivilegesService {
    override fun canProvideAccNo(email: String) = isSuperUser(email)

    override fun canSubmitProjects(email: String) = isSuperUser(email)

    override fun canResubmit(email: String, author: String, project: String?, accessTags: List<String>) =
        isSuperUser(email)
            .or(isAuthor(author, email).and(isNotInProject(project)))
            .or(hasTag(accessTags, AccessType.SUBMIT))

    override fun canDelete(email: String, author: String, accessTags: List<String>) =
        isSuperUser(email)
            .or(isAuthor(author, email))
            .or(hasTag(accessTags, AccessType.DELETE))

    private fun isNotInProject(project: String?) = project.isNullOrBlank()

    private fun hasTag(accessTags: List<String>, accessType: AccessType) =
        accessPermissionRepository.existsByAccessTagNameInAndAccessType(accessTags, accessType)

    private fun isSuperUser(email: String) = getUser(email).superuser

    private fun isAuthor(author: String, email: String) = author == email

    private fun getUser(email: String) =
        userRepository.findByEmail(email).orElseThrow { UserNotFoundByEmailException(email) }
}
