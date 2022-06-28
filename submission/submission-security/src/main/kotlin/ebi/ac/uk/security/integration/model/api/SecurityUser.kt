package ebi.ac.uk.security.integration.model.api

import ac.uk.ebi.biostd.persistence.common.model.AccessType
import ebi.ac.uk.model.User
import java.nio.file.Path

data class SecurityUser(
    val id: Long,
    val email: String,
    val fullName: String,
    val login: String?,
    val orcid: String?,
    val secret: String,
    val superuser: Boolean,
    val magicFolder: MagicFolder,
    val groupsFolders: List<GroupMagicFolder>,
    val permissions: Set<SecurityPermission>,
    val notificationsEnabled: Boolean,
) {
    fun asUser() = User(id, email, secret, fullName, notificationsEnabled)
}

data class SecurityPermission(val accessType: AccessType, val accessTag: String)

data class MagicFolder(val relativePath: Path, val path: Path) {
    fun resolve(subPath: String): Path = path.resolve(subPath)
}

data class GroupMagicFolder(val groupName: String, val path: Path, val description: String? = null)
