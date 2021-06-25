package ac.uk.ebi.biostd.security.domain.service

import ac.uk.ebi.biostd.persistence.common.model.AccessType
import ac.uk.ebi.biostd.persistence.model.DbAccessPermission
import ac.uk.ebi.biostd.persistence.repositories.AccessPermissionRepository
import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepo
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ac.uk.ebi.biostd.security.domain.exception.PermissionsAccessTagDoesNotExistsException
import ac.uk.ebi.biostd.security.domain.exception.PermissionsUserDoesNotExistsException

class PermissionService(
    private val permissionRepository: AccessPermissionRepository,
    private val userDataRepository: UserDataRepository,
    private val accessTagDataRepository: AccessTagDataRepo
) {
    fun givePermissionToUser(accessType: String, email: String, accessTagName: String) {
        val user = userDataRepository.findByEmail(email)
        user.orElseThrow { throw PermissionsUserDoesNotExistsException(email) }
        val accessTag = requireNotNull(accessTagDataRepository.findByName(accessTagName)) {
            throw PermissionsAccessTagDoesNotExistsException(accessTagName)
        }

        if (permissionExists(accessType, email, accessTagName).not()) {
            permissionRepository.save(
                DbAccessPermission(accessType = toAccessType(accessType), user = user.get(), accessTag = accessTag)
            )
        }
    }

    private fun permissionExists(accessType: String, email: String, accessTagName: String): Boolean =
        permissionRepository.existsByUserEmailAndAccessTypeAndAccessTagName(
            email,
            toAccessType(accessType),
            accessTagName
        )

    private fun toAccessType(accessType: String): AccessType = AccessType.valueOf(accessType.toUpperCase())
}
