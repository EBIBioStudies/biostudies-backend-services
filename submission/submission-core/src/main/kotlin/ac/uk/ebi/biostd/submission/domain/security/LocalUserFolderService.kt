package ac.uk.ebi.biostd.submission.domain.security

import ac.uk.ebi.biostd.common.properties.SecurityProperties
import ac.uk.ebi.biostd.common.properties.StorageMode
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ebi.ac.uk.io.FileUtils
import ebi.ac.uk.io.RWXRWX___
import ebi.ac.uk.io.RWX__X___
import ebi.ac.uk.model.MigrateHomeOptions
import ebi.ac.uk.security.integration.components.SecurityQueryService
import ebi.ac.uk.security.integration.exception.UserNotFoundByEmailException
import ebi.ac.uk.security.integration.model.api.FtpUserFolder
import ebi.ac.uk.security.integration.model.api.NfsUserFolder
import ebi.ac.uk.security.integration.model.api.SecurityUser
import ebi.ac.uk.security.service.ProfileService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.PosixFilePermissions

private val logger = KotlinLogging.logger {}

class LocalUserFolderService(
    private val securityQueryService: SecurityQueryService,
    private val userRepository: UserDataRepository,
    private val profileService: ProfileService,
    private val props: SecurityProperties,
) {
    suspend fun updateMagicFolder(
        email: String,
        migrateOptions: MigrateHomeOptions,
    ) {
        //val stats = securityQueryService.getUserFolderInventory(email)
        //if (migrateOptions.onlyIfEmptyFolder && stats.nonSubmissionFiles > 0) error("$email is not empty and can not be migrated")
        updateMagicFolder(
            email,
            StorageMode.valueOf(migrateOptions.storageMode),
            migrateOptions.copyFilesSinceDays,
        )
    }

    private suspend fun updateMagicFolder(
        email: String,
        storageMode: StorageMode,
        days: Int,
    ): Unit =
        withContext(Dispatchers.IO) {
            val user = userRepository.findByEmail(email) ?: throw UserNotFoundByEmailException(email)
            if (user.storageMode == storageMode) error("User '$email' Storage is already $storageMode")

            val source = profileService.asSecurityUser(user)
            val target = profileService.asSecurityUser(user.apply { this.storageMode = storageMode })

            createMagicFolder(target)
            userRepository.save(user)
            copyFilesClusterJob(source.userFolder.path, target.userFolder.path, days)
        }

    private suspend fun createMagicFolder(user: SecurityUser) {
        when (val folder = user.userFolder) {
            is FtpUserFolder -> createFtpMagicFolder(folder)
            is NfsUserFolder -> createNfsMagicFolder(user.email, folder)
        }
    }

    private suspend fun createFtpMagicFolder(ftpFolder: FtpUserFolder) {
        createClusterFolder(ftpFolder.path.parent, UNIX_RWX__X___)
        createClusterFolder(ftpFolder.path, UNIX_RWXRWX___)
    }

    private fun createNfsMagicFolder(
        email: String,
        nfsFolder: NfsUserFolder,
    ) {
        FileUtils.getOrCreateFolder(nfsFolder.path.parent, RWX__X___)
        FileUtils.getOrCreateFolder(nfsFolder.path, RWXRWX___)
        FileUtils.createSymbolicLink(symLinkPath(email), nfsFolder.path, RWXRWX___)
    }

    private fun symLinkPath(userEmail: String): Path {
        val prefixFolder = userEmail.substring(0, 1).lowercase()
        return Paths.get("${props.filesProperties.magicDirPath}/$prefixFolder/$userEmail")
    }

    private suspend fun createClusterFolder(
        path: Path,
        permissions: String,
    ) = withContext(Dispatchers.IO) {
        logger.info { "Started creating the cluster FTP folder $path" }
        Files.createDirectories(path)
        Files.setPosixFilePermissions(path, PosixFilePermissions.fromString(permissions))
        logger.info { "Finished creating the cluster FTP folder $path" }
    }

    private suspend fun copyFilesClusterJob(
        source: Path,
        target: Path,
        days: Int,
    ) = withContext(Dispatchers.IO) {
        val rsyncCommand = """
        rsync -av \
        --files-from=<(find "$source" -mtime -$days -type f | sed "s|^$source/||") \
        "$source" "$target"
    """.trimIndent()

        logger.info { "Executing rsync command: '$rsyncCommand'" }
        executeCommand(rsyncCommand)

        val permsCommand = """
    chgrp -R biostudies "$target" && \
    find "$target" -type f -exec chmod 660 {} + && \
    find "$target" -type d -exec chmod 770 {} +
""".trimIndent()

        logger.info { "Fixing permissions: '$permsCommand'" }
        executeCommand(permsCommand)
    }

    private fun executeCommand(command: String) {
        val pb = ProcessBuilder("bash", "-c", command)
        pb.redirectErrorStream(true)
        val process = pb.start()
        process.inputStream.bufferedReader().useLines { lines -> lines.forEach { logger.info { it } } }
        val exitCode = process.waitFor()
        logger.info { "Finished executing command. Exit code: $exitCode" }
    }

    companion object {
        internal const val UNIX_RWX__X___ = "rwx--x---" // 710
        internal const val UNIX_RWXRWX___ = "rwxrwx---" // 770
    }
}
