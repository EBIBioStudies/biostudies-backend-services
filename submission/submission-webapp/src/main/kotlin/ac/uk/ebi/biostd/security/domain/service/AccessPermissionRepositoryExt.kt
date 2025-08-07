package ac.uk.ebi.biostd.security.domain.service

import ac.uk.ebi.biostd.persistence.common.model.AccessType
import ac.uk.ebi.biostd.persistence.repositories.AccessPermissionRepository

fun AccessPermissionRepository.permissionExists(
    accessType: AccessType,
    email: String,
    accessTagName: String,
): Boolean =
    existsByUserEmailAndAccessTypeAndAccessTagName(
        email,
        accessType,
        accessTagName,
    )
