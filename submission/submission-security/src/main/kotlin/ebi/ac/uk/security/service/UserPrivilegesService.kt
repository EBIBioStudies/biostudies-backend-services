package ebi.ac.uk.security.service

import ac.uk.ebi.biostd.persistence.model.AccessType
import ac.uk.ebi.biostd.persistence.repositories.AccessPermissionRepository
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ebi.ac.uk.model.User
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import ebi.ac.uk.security.integration.exception.UserNotFoundByEmailException

internal class UserPrivilegesService(
    private val userRepository: UserDataRepository,
    private val accessPermissionRepository: AccessPermissionRepository
) : IUserPrivilegesService {
    override fun canProvideAccNo(email: String) = isSuperUser(email)

    override fun canResubmit(email: String, author: User, project: String?, accessTags: List<String>): Boolean {
        return isSuperUser(email)
            .or(isAuthor(author, email).and(isNotInProject(project)))
            .or(hasTag(accessTags, AccessType.SUBMIT))
    }

    // TODO: add proper security validation
    override fun canDelete(accNo: String, email: String): Boolean {
        return true
    }

    private fun isNotInProject(project: String?) = project.isNullOrBlank()

    private fun hasTag(accessTags: List<String>, accessType: AccessType) =
        accessPermissionRepository.existsByAccessTagInAndAccessType(accessTags, accessType)

    private fun isSuperUser(email: String) = getUser(email).superuser

    private fun isAuthor(author: User, email: String) = getUser(email).id == author.id

    private fun getUser(email: String) =
        userRepository
            .findByEmailAndActive(email, true)
            .orElseThrow { UserNotFoundByEmailException(email) }
}
