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
import org.springframework.transaction.annotation.Transactional
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.PosixFilePermissions

private val logger = KotlinLogging.logger {}

open class LocalUserFolderService(
    private val securityQueryService: SecurityQueryService,
    private val userRepository: UserDataRepository,
    private val profileService: ProfileService,
    private val props: SecurityProperties,
) {
    @Transactional
    suspend fun updateMagicFolder(
        email: String,
        migrateOptions: MigrateHomeOptions,
    ) {
        val stats = securityQueryService.getUserFolderStats(email)
        if (migrateOptions.onlyIfEmptyFolder && stats.totalFiles > 0) error("$email is not empty and can not be migrated")
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
        val command =
            """
            rsync -av \
            --files-from=<(find $source -mtime -$days | sed "s|^$source/||") $source $target \
            && echo "rsync exit code: 0" || echo "rsync exit code: $?" \\
            && chgrp -R biostudies $target
            """.trimIndent()

        logger.info { "Executing command '$command'" }

        val pb = ProcessBuilder("bash", "-c", command)
        pb.redirectErrorStream(true)
        val process = pb.start()
        val exitCode = process.waitFor()
        logger.info { "Finished copying files to the cluster FTP folder $target from $source. Exit code: $exitCode" }
    }

    companion object {
        internal const val UNIX_RWX__X___ = "rwx--x---" // 710
        internal const val UNIX_RWXRWX___ = "rwxrwx---" // 770
    }
}
