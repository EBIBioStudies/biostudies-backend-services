package ac.uk.ebi.biostd.security.domain.service

import ac.uk.ebi.biostd.persistence.common.model.AccessType
import ac.uk.ebi.biostd.persistence.repositories.AccessPermissionRepository
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ac.uk.ebi.biostd.security.domain.exception.PermissionsUserDoesNotExistsException
import ac.uk.ebi.biostd.security.domain.exception.RevokePermissionException
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import kotlinx.coroutines.runBlocking
import org.springframework.transaction.annotation.Transactional

/**
 * Separate class required due to @Transactional annotation
 */
open class RevokePermissionService(
    private val userRepository: UserDataRepository,
    private val permissionRepository: AccessPermissionRepository,
    private val userPrivilegesService: IUserPrivilegesService,
) {
    @Transactional
    open fun revokePermission(
        user: String,
        accessType: AccessType,
        targetUser: String,
        accNo: String,
    ) {
        runBlocking {
            require(userPrivilegesService.canRevokePermissions(user, accNo)) {
                throw RevokePermissionException(user, accNo)
            }
        }
        require(userRepository.existsByEmail(targetUser)) { throw PermissionsUserDoesNotExistsException(targetUser) }

        if (permissionRepository.permissionExists(accessType, targetUser, accNo)) {
            permissionRepository.deleteByUserEmailAndAccessTypeAndAccessTagName(targetUser, accessType, accNo)
        }
    }
}
