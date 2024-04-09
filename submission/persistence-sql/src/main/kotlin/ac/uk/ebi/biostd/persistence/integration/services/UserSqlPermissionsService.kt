package ac.uk.ebi.biostd.persistence.integration.services

import ac.uk.ebi.biostd.persistence.common.model.AccessType
import ac.uk.ebi.biostd.persistence.common.model.AccessType.ADMIN
import ac.uk.ebi.biostd.persistence.common.service.UserPermissionsService
import ac.uk.ebi.biostd.persistence.model.DbAccessPermission
import ac.uk.ebi.biostd.persistence.repositories.AccessPermissionRepository

const val DEFAULT_USER = "default_user@ebi.ac.uk"

internal class UserSqlPermissionsService(
    private val permissionRepo: AccessPermissionRepository,
) : UserPermissionsService {
    override fun allowedTags(
        user: String,
        accessType: AccessType,
    ): List<DbAccessPermission> =
        permissionRepo.findAllByUserEmailAndAccessType(user, accessType) +
            permissionRepo.findAllByUserEmailAndAccessType(user, ADMIN) +
            permissionRepo.findAllByUserEmailAndAccessType(DEFAULT_USER, accessType)

    override fun isAdmin(
        user: String,
        accessTag: String,
    ): Boolean {
        return permissionRepo.existsByUserEmailAndAccessTypeAndAccessTagName(user, ADMIN, accessTag)
    }

    override fun hasPermission(
        user: String,
        accessTag: String,
        accessType: AccessType,
    ): Boolean =
        permissionRepo.existsByUserEmailAndAccessTypeAndAccessTagName(user, accessType, accessTag) ||
            permissionRepo.existsByUserEmailAndAccessTypeAndAccessTagName(DEFAULT_USER, accessType, accessTag)
}
