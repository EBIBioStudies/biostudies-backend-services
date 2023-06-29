package ebi.ac.uk.security.service

import ac.uk.ebi.biostd.common.properties.StorageMode
import ac.uk.ebi.biostd.persistence.model.DbAccessPermission
import ac.uk.ebi.biostd.persistence.model.DbUser
import ac.uk.ebi.biostd.persistence.model.DbUserGroup
import ebi.ac.uk.security.integration.model.api.FtpUserFolder
import ebi.ac.uk.security.integration.model.api.GroupFolder
import ebi.ac.uk.security.integration.model.api.NfsUserFolder
import ebi.ac.uk.security.integration.model.api.SecurityPermission
import ebi.ac.uk.security.integration.model.api.SecurityUser
import ebi.ac.uk.security.integration.model.api.UserFolder
import ebi.ac.uk.security.integration.model.api.UserInfo
import java.nio.file.Path
import java.nio.file.Paths

class ProfileService(private val filesDirPath: Path) {
    fun getUserProfile(user: DbUser, token: String): UserInfo = UserInfo(asSecurityUser(user), token)

    fun asSecurityUser(user: DbUser): SecurityUser = SecurityUser(
        id = user.id,
        email = user.email,
        fullName = user.fullName,
        login = user.login,
        orcid = user.orcid,
        secret = user.secret,
        superuser = user.superuser,
        userFolder = userMagicFolder(user.storageMode, user.secret, user.id),
        groupsFolders = groupsMagicFolder(user.groups),
        permissions = getPermissions(user.permissions),
        notificationsEnabled = user.notificationsEnabled
    )

    private fun getPermissions(permissions: Set<DbAccessPermission>): Set<SecurityPermission> =
        permissions.mapTo(mutableSetOf()) { SecurityPermission(it.accessType, it.accessTag.name) }

    private fun groupsMagicFolder(groups: Set<DbUserGroup>): List<GroupFolder> =
        groups.map { GroupFolder(it.name, groupMagicFolder(it), it.description) }

    private fun groupMagicFolder(it: DbUserGroup) = Paths.get("$filesDirPath/${magicPath(it.secret, it.id, "b")}")

    private fun userMagicFolder(folderType: StorageMode, secret: String, id: Long): UserFolder {
        fun nfsFolder(): NfsUserFolder {
            val relativePath = magicPath(secret, id, "a")
            return NfsUserFolder(Paths.get(relativePath), Paths.get("$filesDirPath/$relativePath"))
        }

        fun ftpFolder(): FtpUserFolder {
            val relativePath = magicPath(secret, id, "a")
            return FtpUserFolder(Paths.get(relativePath))
        }

        return when (folderType) {
            StorageMode.FTP -> ftpFolder()
            StorageMode.NFS -> nfsFolder()
        }
    }

    private fun magicPath(secret: String, id: Long, suffix: String) = "${secret.take(2)}/${secret.drop(2)}-$suffix$id"
}
