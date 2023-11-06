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
    val userFolder: UserFolder,
    val groupsFolders: List<GroupFolder>,
    val permissions: Set<SecurityPermission>,
    val notificationsEnabled: Boolean,
) {
    fun asUser() = User(id, email, secret, fullName, notificationsEnabled)
}

data class SecurityPermission(val accessType: AccessType, val accessTag: String)

sealed interface UserFolder {
    val relativePath: Path
    val path: Path
}

data class FtpUserFolder(
    /**
     * The user ftp path. Used to access files through ftp protocol.
     */
    override val relativePath: Path,

    /**
     * The File system ftp path. Used to access ftp files as file system file.
     */
    override val path: Path,
) : UserFolder

data class NfsUserFolder(override val relativePath: Path, override val path: Path) : UserFolder

fun NfsUserFolder.resolve(subPath: String): Path = path.resolve(subPath)

data class GroupFolder(val groupName: String, val path: Path, val description: String? = null)
