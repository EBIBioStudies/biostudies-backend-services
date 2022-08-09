package ebi.ac.uk.security.service

import ac.uk.ebi.biostd.persistence.model.DbAccessPermission
import ac.uk.ebi.biostd.persistence.model.DbUser
import ac.uk.ebi.biostd.persistence.model.DbUserGroup
import ebi.ac.uk.security.integration.model.api.GroupMagicFolder
import ebi.ac.uk.security.integration.model.api.MagicFolder
import ebi.ac.uk.security.integration.model.api.SecurityPermission
import ebi.ac.uk.security.integration.model.api.SecurityUser
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
        magicFolder = userMagicFolder(user.secret, user.id),
        groupsFolders = groupsMagicFolder(user.groups),
        permissions = getPermissions(user.permissions),
        notificationsEnabled = user.notificationsEnabled
    )

    private fun getPermissions(permissions: Set<DbAccessPermission>): Set<SecurityPermission> =
        permissions.mapTo(mutableSetOf()) { SecurityPermission(it.accessType, it.accessTag.name) }

    private fun groupsMagicFolder(groups: Set<DbUserGroup>): List<GroupMagicFolder> =
        groups.map { GroupMagicFolder(it.name, groupMagicFolder(it), it.description) }

    private fun groupMagicFolder(it: DbUserGroup) = Paths.get("$filesDirPath/${magicPath(it.secret, it.id, "b")}")

    private fun userMagicFolder(secret: String, id: Long): MagicFolder {
        val relativePath = magicPath(secret, id, "a")
        return MagicFolder(Paths.get(relativePath), Paths.get("$filesDirPath/$relativePath"))
    }

    private fun magicPath(secret: String, id: Long, suffix: String) = "${secret.take(2)}/${secret.drop(2)}-$suffix$id"
}
