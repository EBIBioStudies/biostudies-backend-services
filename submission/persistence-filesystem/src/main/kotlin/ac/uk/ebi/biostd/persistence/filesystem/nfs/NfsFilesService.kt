package ac.uk.ebi.biostd.persistence.filesystem.nfs

import ac.uk.ebi.biostd.persistence.filesystem.api.FilesService
import ac.uk.ebi.biostd.persistence.filesystem.extensions.SubPermissions
import ac.uk.ebi.biostd.persistence.filesystem.extensions.permissions
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmissionInfo
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.extended.model.RequestFile
import ebi.ac.uk.extended.model.asNfsFile
import ebi.ac.uk.io.FileUtils
import ebi.ac.uk.io.FileUtils.copyOrReplaceFile
import ebi.ac.uk.io.FileUtils.getOrCreateFolder
import ebi.ac.uk.io.ext.notExist
import ebi.ac.uk.paths.SubmissionFolderResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import uk.ac.ebi.fire.client.integration.web.FireClient
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.PosixFilePermission
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists

private val logger = KotlinLogging.logger {}

class NfsFilesService(
    private val fireClient: FireClient,
    private val folderResolver: SubmissionFolderResolver,
) : FilesService {
    override suspend fun persistSubmissionFile(
        sub: ExtSubmissionInfo,
        file: ExtFile,
    ): ExtFile =
        withContext(Dispatchers.IO) {
            when (file) {
                is FireFile -> persistFireFile(sub, file)
                is NfsFile -> persistNfsFile(sub, file)
                is RequestFile -> error("RequestFile ${file.filePath} can not be persisted")
            }
        }

    private fun persistNfsFile(
        sub: ExtSubmissionInfo,
        file: NfsFile,
    ): ExtFile {
        val subPermissions = sub.permissions()
        val subFile = getSubFile(sub, subPermissions, file.relPath)
        if (subFile.notExist()) copyOrReplaceFile(file.file, subFile, subPermissions.asPermissions())

        return file.copy(fullPath = subFile.absolutePath, file = subFile)
    }

    private suspend fun persistFireFile(
        sub: ExtSubmissionInfo,
        file: FireFile,
    ): ExtFile {
        val subPermissions = sub.permissions()
        val subFile = getSubFile(sub, subPermissions, file.relPath)
        if (subFile.notExist()) {
            val fireFile = fireClient.downloadByPath(file.firePath)!!
            copyOrReplaceFile(fireFile, subFile, subPermissions.asPermissions())
        }

        return file.asNfsFile(subFile)
    }

    private fun getSubFile(
        sub: ExtSubmissionInfo,
        permissions: SubPermissions,
        relPath: String,
    ): File {
        val subFolder = getOrCreateSubmissionFolder(sub, permissions.parentsFolder, permissions.subFolder)
        return subFolder.resolve(relPath)
    }

    private fun getOrCreateSubmissionFolder(
        sub: ExtSubmissionInfo,
        parentsFolder: Set<PosixFilePermission>,
        subFolder: Set<PosixFilePermission>,
    ): File {
        val submissionPath = folderResolver.getPrivateSubFolder(sub.secretKey, sub.relPath)
        FileUtils.createParentFolders(submissionPath, parentsFolder)
        return getOrCreateFolder(submissionPath, subFolder).toFile()
    }

    override suspend fun deleteSubmissionFile(
        sub: ExtSubmissionInfo,
        file: ExtFile,
    ) = withContext(Dispatchers.IO) {
        require(file is NfsFile) { "NfsFilesService should only handle NfsFile" }

        val subDirectory = folderResolver.getPrivateSubFolder(sub.secretKey, sub.relPath)
        val toDeleteFile = subDirectory.resolve(file.relPath).toFile()

        logger.info { "${sub.accNo} ${sub.owner} deleting submission File '${toDeleteFile.absolutePath}' on NFS" }
        FileUtils.deleteFile(toDeleteFile)
        logger.info { "${sub.accNo} ${sub.owner} Finished deleting '${toDeleteFile.absolutePath}' on NFS" }
    }

    override suspend fun deleteFtpFile(
        sub: ExtSubmissionInfo,
        file: ExtFile,
    ) = withContext(Dispatchers.IO) {
        require(file is NfsFile) { "NfsFilesService should only handle NfsFile" }

        val subFolder = folderResolver.getPublicSubFolder(sub.relPath)
        val toDeleteFile = subFolder.resolve(file.relPath).toFile()

        logger.info { "${sub.accNo} ${sub.owner} deleting File '${toDeleteFile.absolutePath}' on NFS" }
        FileUtils.deleteFile(toDeleteFile)
        logger.info { "${sub.accNo} ${sub.owner} Finished deleting '${toDeleteFile.absolutePath}' on NFS" }
    }

    override suspend fun deleteEmptyFolders(sub: ExtSubmissionInfo) =
        withContext(Dispatchers.IO) {
            val subFolder = folderResolver.getPrivateSubFolder(sub.secretKey, sub.relPath).toFile()
            logger.info { "${sub.accNo} ${sub.owner} Deleting sub empty folders in ${subFolder.parentFile.absolutePath}" }
            FileUtils.deleteEmptyDirectories(subFolder)
        }

    override suspend fun copyFile(
        file: ExtFile,
        path: Path,
    ) {
        withContext(Dispatchers.IO) {
            require(file is NfsFile) { "NfsFilesService should only handle NfsFile" }
            path.deleteIfExists()
            path.createDirectories()
            Files.copy(Paths.get(file.fullPath), path)
        }
    }
}
