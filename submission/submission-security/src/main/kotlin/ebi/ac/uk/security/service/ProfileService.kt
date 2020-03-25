package ebi.ac.uk.security.service

import ac.uk.ebi.biostd.persistence.model.DbUser
import ac.uk.ebi.biostd.persistence.model.UserGroup
import ebi.ac.uk.security.integration.model.api.GroupMagicFolder
import ebi.ac.uk.security.integration.model.api.MagicFolder
import ebi.ac.uk.security.integration.model.api.SecurityUser
import ebi.ac.uk.security.integration.model.api.UserInfo
import java.nio.file.Path
import java.nio.file.Paths

class ProfileService(private val filesDirPath: Path) {
    fun getUserProfile(user: DbUser, token: String): UserInfo {
        return UserInfo(asSecurityUser(user), token)
    }

    fun asSecurityUser(user: DbUser): SecurityUser {
        return user.run {
            SecurityUser(
                id = id,
                email = email,
                fullName = fullName,
                login = login,
                secret = secret,
                superuser = user.superuser,
                magicFolder = userMagicFolder(secret, id),
                groupsFolders = groupsMagicFolder(user.groups),
                permissions = permissions)
        }
    }

    private fun groupsMagicFolder(groups: Set<UserGroup>): List<GroupMagicFolder> =
        groups.map { GroupMagicFolder(it.name, userMagicFolder(it), it.description) }

    private fun userMagicFolder(it: UserGroup) = Paths.get("$filesDirPath/${magicPath(it.secret, it.id, "b")}")

    private fun userMagicFolder(secret: String, id: Long): MagicFolder {
        val relativePath = magicPath(secret, id, "a")
        return MagicFolder(Paths.get(relativePath), Paths.get("$filesDirPath/$relativePath"))
    }

    private fun magicPath(secret: String, id: Long, suffix: String) = "${secret.take(2)}/${secret.drop(2)}-$suffix$id"
}
