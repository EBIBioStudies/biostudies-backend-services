package ac.uk.ebi.biostd.persistence.common.service

import ac.uk.ebi.biostd.persistence.common.model.AccessPermission
import ac.uk.ebi.biostd.persistence.common.model.AccessType

interface UserPermissionsService {
    fun allowedTags(user: String, accessType: AccessType): List<AccessPermission>

    fun isAdmin(user: String, accessTag: String): Boolean

    fun hasPermission(user: String, accessTag: String, accessType: AccessType): Boolean
}
