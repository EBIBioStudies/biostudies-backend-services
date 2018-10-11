package ebi.ac.uk.paths

import java.nio.file.Path
import java.nio.file.Paths

internal const val USER_FOLDER_PREFIX = "a"
internal const val GROUP_FOLDER_PREFIX = "b"

class FolderResolver(private val basePath: String) {

    fun getUserMagicFolderPath(userId: Long, secret: String): Path {
        return getMagicFolderPath(userId, secret, USER_FOLDER_PREFIX)
    }

    fun getGroupMagicFolderPath(groupId: Long, secret: String): Path {
        return getMagicFolderPath(groupId, secret, GROUP_FOLDER_PREFIX)
    }

    private fun getMagicFolderPath(id: Long, secret: String, separator: String): Path {
        val parent = "$basePath/${secret.substring(0, 2)}"
        return Paths.get("$parent/${secret.substring(2)}-$separator$id")
    }
}