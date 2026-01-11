package ebi.ac.uk.security.service

import ac.uk.ebi.biostd.common.properties.StorageMode
import ac.uk.ebi.biostd.persistence.common.model.AccessType
import ac.uk.ebi.biostd.persistence.common.service.SubmissionFilesPersistenceService
import ac.uk.ebi.biostd.persistence.model.DbAccessPermission
import ac.uk.ebi.biostd.persistence.model.DbUser
import ac.uk.ebi.biostd.persistence.model.DbUserGroup
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.model.FolderInventory
import ebi.ac.uk.model.FolderStats
import ebi.ac.uk.model.InventoryFile
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import ebi.ac.uk.security.integration.model.api.FtpUserFolder
import ebi.ac.uk.security.integration.model.api.GroupFolder
import ebi.ac.uk.security.integration.model.api.NfsUserFolder
import ebi.ac.uk.security.integration.model.api.SecurityPermission
import ebi.ac.uk.security.integration.model.api.SecurityUser
import ebi.ac.uk.security.integration.model.api.UserFolder
import ebi.ac.uk.security.integration.model.api.UserInfo
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Instant

@Suppress("TooManyFunctions")
class ProfileService(
    private val userFtpDirPath: Path,
    private val nfsUserFilesDirPath: Path,
    private val userFtpRootPath: String,
    private val privilegesService: IUserPrivilegesService,
    private val filesPersistenceService: SubmissionFilesPersistenceService,
) {
    fun getUserProfile(
        user: DbUser,
        token: String,
    ): UserInfo = UserInfo(asSecurityUser(user), token)

    fun asSecurityUser(user: DbUser): SecurityUser =
        SecurityUser(
            id = user.id,
            email = user.email,
            fullName = user.fullName,
            login = user.login,
            orcid = user.orcid,
            secret = user.secret,
            superuser = user.superuser,
            userFolder = userMagicFolder(user.storageMode, userFtpRootPath, user.secret, user.id),
            groupsFolders = groupsMagicFolder(user.groups),
            permissions = getPermissions(user.permissions),
            adminCollections = privilegesService.allowedCollections(user.email, AccessType.ADMIN),
            notificationsEnabled = user.notificationsEnabled,
        )

    suspend fun getUserFolderStats(user: DbUser): FolderStats {
        val profile = asSecurityUser(user)
        return when (val userFolder = profile.userFolder) {
            is FtpUserFolder -> error("Ftp user folder not supported")
            is NfsUserFolder -> calculateFolderStats(userFolder.path.toFile())
        }
    }

    suspend fun getUserFolderInventory(user: DbUser): FolderInventory {
        val profile = asSecurityUser(user)
        return when (val userFolder = profile.userFolder) {
            is FtpUserFolder -> error("Ftp user folder not supported")
            is NfsUserFolder -> calculateFolderInventory(userFolder.path.toFile())
        }
    }

    private suspend fun calculateFolderInventory(nfsUserFolder: File): FolderInventory {
        val files =
            nfsUserFolder
                .walk()
                .drop(1)
                .asFlow()
                .filter { it.isFile }
                .map {
                    val md5 = it.md5()
                    InventoryFile(it.path, it.length(), md5, filesPersistenceService.findSubmissionFile(md5))
                }.toList()
        return FolderInventory(files)
    }

    private suspend fun calculateFolderStats(nfsUserFolder: File): FolderStats {
        var totalFiles = 0
        var totalSize = 0L
        var totalDirectories = 0
        var latestModificationTime = 0L

        nfsUserFolder
            .walk()
            .drop(1)
            .asFlow()
            .onEach { if (it.isFile) totalFiles++ else totalDirectories++ }
            .filter { it.isFile }
            .collect { file ->
                totalSize += file.length()
            }

        return FolderStats(
            totalFiles = totalFiles,
            totalDirectories = totalDirectories,
            totalFilesSize = totalSize,
            lastModification = Instant.ofEpochMilli(latestModificationTime),
        )
    }

    private fun getPermissions(permissions: Set<DbAccessPermission>): Set<SecurityPermission> =
        permissions.mapTo(mutableSetOf()) { SecurityPermission(it.accessType, it.accessTag.name) }

    private fun groupsMagicFolder(groups: Set<DbUserGroup>): List<GroupFolder> =
        groups.map { GroupFolder(it.name, groupMagicFolder(it), it.description) }

    private fun groupMagicFolder(it: DbUserGroup) = Paths.get("$nfsUserFilesDirPath/${magicPath(it.secret, it.id, "b")}")

    private fun userMagicFolder(
        folderType: StorageMode,
        ftpRootPath: String,
        secret: String,
        id: Long,
    ): UserFolder {
        fun nfsFolder(): NfsUserFolder {
            val relativePath = magicPath(secret, id, "a")
            return NfsUserFolder(Paths.get(relativePath), Paths.get("$nfsUserFilesDirPath/$relativePath"))
        }

        fun ftpFolder(): FtpUserFolder {
            val magicPath = magicPath(secret, id, "a")
            val relativePath = if (ftpRootPath.isBlank()) magicPath else "$ftpRootPath/$magicPath"
            return FtpUserFolder(
                relativePath = Paths.get(relativePath),
                path = Paths.get("$userFtpDirPath").resolve(relativePath),
            )
        }

        return when (folderType) {
            StorageMode.FTP -> ftpFolder()
            StorageMode.NFS -> nfsFolder()
        }
    }

    private fun magicPath(
        secret: String,
        id: Long,
        suffix: String,
    ) = "${secret.take(2)}/${secret.drop(2)}-$suffix$id"
}
