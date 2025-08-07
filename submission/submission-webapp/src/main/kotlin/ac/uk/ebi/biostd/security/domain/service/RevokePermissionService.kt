package ac.uk.ebi.biostd.security.domain.service

import ac.uk.ebi.biostd.persistence.common.model.AccessType
import ac.uk.ebi.biostd.persistence.repositories.AccessPermissionRepository
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ac.uk.ebi.biostd.security.domain.exception.PermissionsUserDoesNotExistsException
import org.springframework.transaction.annotation.Transactional

open class RevokePermissionService(
    private val userRepository: UserDataRepository,
    private val permissionRepository: AccessPermissionRepository,
) {
    @Transactional
    open fun revokePermission(
        accessType: AccessType,
        email: String,
        accNo: String,
    ) {
        require(userRepository.existsByEmail(email)) { throw PermissionsUserDoesNotExistsException(email) }

        if (permissionRepository.permissionExists(accessType, email, accNo)) {
            permissionRepository.deleteByUserEmailAndAccessTypeAndAccessTagName(email, accessType, accNo)
        }
    }
}
