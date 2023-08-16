package ac.uk.ebi.biostd.persistence.filesystem.nfs

import ac.uk.ebi.biostd.persistence.filesystem.api.FilesService
import ac.uk.ebi.biostd.persistence.filesystem.extensions.FilePermissionsExtensions.permissions
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.extended.model.asNfsFile
import ebi.ac.uk.io.FileUtils
import ebi.ac.uk.io.FileUtils.copyOrReplaceFile
import ebi.ac.uk.io.FileUtils.getOrCreateFolder
import ebi.ac.uk.io.Permissions
import ebi.ac.uk.io.RWXR_XR_X
import ebi.ac.uk.io.ext.notExist
import ebi.ac.uk.paths.SubmissionFolderResolver
import mu.KotlinLogging
import uk.ac.ebi.fire.client.integration.web.FireClient
import java.io.File
import java.nio.file.attribute.PosixFilePermission

private val logger = KotlinLogging.logger {}

class NfsFilesService(
    private val fireClient: FireClient,
    private val folderResolver: SubmissionFolderResolver,
) : FilesService {
    override suspend fun persistSubmissionFile(sub: ExtSubmission, file: ExtFile): ExtFile {
        return when (file) {
            is FireFile -> persistFireFile(sub, file)
            is NfsFile -> persistNfsFile(sub, file)
        }
    }

    private fun persistNfsFile(sub: ExtSubmission, file: NfsFile): ExtFile {
        val permissions = sub.permissions()
        val subFile = getSubFile(sub, permissions, file.relPath)
        if (subFile.notExist()) copyOrReplaceFile(file.file, subFile, permissions)

        return file.copy(fullPath = subFile.absolutePath, file = subFile)
    }

    private fun persistFireFile(sub: ExtSubmission, file: FireFile): ExtFile {
        val permissions = sub.permissions()
        val subFile = getSubFile(sub, permissions, file.relPath)
        if (subFile.notExist()) {
            val fireFile = fireClient.downloadByPath(file.firePath!!)!!
            copyOrReplaceFile(fireFile, subFile, permissions)
        }

        return file.asNfsFile(subFile)
    }

    private fun getSubFile(sub: ExtSubmission, permissions: Permissions, relPath: String): File {
        val subFolder = getOrCreateSubmissionFolder(sub, permissions.folder)
        return subFolder.resolve(relPath)
    }

    private fun getOrCreateSubmissionFolder(submission: ExtSubmission, permissions: Set<PosixFilePermission>): File {
        val submissionPath = folderResolver.getSubFolder(submission.relPath)
        FileUtils.createParentFolders(submissionPath, RWXR_XR_X)
        return getOrCreateFolder(submissionPath, permissions).toFile()
    }

    override suspend fun deleteSubmissionFile(sub: ExtSubmission, file: ExtFile) {
        require(file is NfsFile) { "NfsFilesService should only handle NfsFile" }

        val subDirectory = folderResolver.getSubFolder(sub.relPath)
        val subFile = subDirectory.resolve(file.relPath).toFile()
        FileUtils.deleteFile(subFile)
    }

    override fun deleteFtpFile(sub: ExtSubmission, file: ExtFile) {
        logger.info { "${sub.accNo} ${sub.owner} Started un-publishing files of submission ${sub.accNo} on NFS" }
        val subFolder = folderResolver.getSubmissionFtpFolder(sub.relPath)
        FileUtils.deleteFile(subFolder.resolve(file.relPath).toFile())
        logger.info { "${sub.accNo} ${sub.owner} Finished un-publishing files of submission ${sub.accNo} on NFS" }
    }

    override fun deleteEmptyFolders(current: ExtSubmission) {
        val subFolder = folderResolver.getSubFolder(current.relPath)
        FileUtils.deleteEmptyDirectories(subFolder.toFile())
    }
}
