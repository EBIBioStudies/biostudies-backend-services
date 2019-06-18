package ebi.ac.uk.security.integration.model.api

import ac.uk.ebi.biostd.persistence.model.AccessPermission
import java.nio.file.Path

class SecurityUser(
    val id: Long,
    val email: String,
    val fullName: String,
    val login: String?,
    val secret: String,
    val superuser: Boolean,
    val magicFolder: MagicFolder,
    val groupsFolders: List<GroupMagicFolder>,
    val permissions: Set<AccessPermission>
) {
}

class MagicFolder(val relativePath: Path, val path: Path)
class GroupMagicFolder(val groupName: String, val path: Path)
