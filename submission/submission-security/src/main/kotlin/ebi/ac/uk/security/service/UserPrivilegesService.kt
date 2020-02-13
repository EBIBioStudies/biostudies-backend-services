package ebi.ac.uk.security.service

import ac.uk.ebi.biostd.persistence.model.AccessType
import ac.uk.ebi.biostd.persistence.model.AccessType.ATTACH
import ac.uk.ebi.biostd.persistence.model.AccessType.UPDATE
import ac.uk.ebi.biostd.persistence.repositories.AccessPermissionRepository
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ebi.ac.uk.model.constants.SubFields.PUBLIC_ACCESS_TAG
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import ebi.ac.uk.security.integration.exception.UserNotFoundByEmailException

internal class UserPrivilegesService(
    private val userRepository: UserDataRepository,
    private val permissionRepository: AccessPermissionRepository
) : IUserPrivilegesService {
    override fun canProvideAccNo(email: String) = isSuperUser(email)

    override fun canSubmitProjects(email: String) = isSuperUser(email)

    override fun canSubmitToProject(submitter: String, project: String): Boolean =
        isSuperUser(submitter)
            .or(permissionRepository.existsByUserEmailAndAccessTypeAndAccessTagName(submitter, ATTACH, project))

    override fun canResubmit(submitter: String, author: String, accessTags: List<String>) =
        isSuperUser(submitter).or(isAuthor(author, submitter))
            .or(hasPermissions(submitter, accessTags, UPDATE))

    override fun canDelete(submitter: String, author: String, accessTags: List<String>) =
        isSuperUser(submitter)
            .or(isAuthor(author, submitter))
            .or(hasPermissions(submitter, accessTags, AccessType.DELETE))

    private fun hasPermissions(user: String, accessTags: List<String>, accessType: AccessType): Boolean {
        val tags = accessTags.filter { it != PUBLIC_ACCESS_TAG.value }
        return tags.isNotEmpty() &&
            tags.all { permissionRepository.existsByUserEmailAndAccessTypeAndAccessTagName(user, accessType, it) }
    }

    private fun isSuperUser(email: String) = getUser(email).superuser

    private fun isAuthor(author: String, email: String) = author == email

    private fun getUser(email: String) =
        userRepository.findByEmail(email).orElseThrow { UserNotFoundByEmailException(email) }
}
