package ac.uk.ebi.biostd.persistence.filesystem.nfs

import ac.uk.ebi.biostd.persistence.filesystem.api.FilesService
import ac.uk.ebi.biostd.persistence.filesystem.extensions.FilePermissionsExtensions.permissions
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.extended.model.allPageTabFiles
import ebi.ac.uk.io.FileUtils
import ebi.ac.uk.io.FileUtils.copyOrReplaceFile
import ebi.ac.uk.io.FileUtils.getOrCreateFolder
import ebi.ac.uk.io.FileUtils.moveFile
import ebi.ac.uk.io.RWXR_XR_X
import ebi.ac.uk.io.RWX______
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.notExist
import ebi.ac.uk.paths.SubmissionFolderResolver
import mu.KotlinLogging
import java.io.File
import java.nio.file.attribute.PosixFilePermission

private val logger = KotlinLogging.logger {}

class NfsFilesService(
    private val folderResolver: SubmissionFolderResolver,
) : FilesService {
    override fun persistSubmissionFile(sub: ExtSubmission, file: ExtFile): ExtFile {
        require(file is NfsFile) { "NfsFilesService should only handle NfsFile" }
        val permissions = sub.permissions()
        val subFolder = getOrCreateSubmissionFolder(sub, permissions.folder)
        val target = getOrCreateTempFolder(subFolder, sub.accNo).resolve(file.relPath)
        val subFile = subFolder.resolve(file.relPath)

        if (target.notExist() && subFile.exists() && subFile.md5() == file.md5)
            moveFile(subFile, target, permissions)
        else if (target.notExist())
            copyOrReplaceFile(file.file, target, permissions)

        return file.copy(fullPath = subFile.absolutePath, file = subFile)
    }

    override fun postProcessSubmissionFiles(sub: ExtSubmission) {
        val permissions = sub.permissions()
        val subFolder = getOrCreateSubmissionFolder(sub, permissions.folder)
        val targetFolder = getOrCreateTempFolder(subFolder, sub.accNo)

        moveFile(targetFolder, subFolder, permissions)
    }

    override fun cleanSubmissionFiles(sub: ExtSubmission) {
        cleanFtpFolder(sub)
        cleanSubFolder(sub)
    }

    override fun cleanCommonFiles(new: ExtSubmission, current: ExtSubmission) {
        cleanFtpFolder(current)
        cleanPageTab(current)
    }

    override fun cleanRemainingFiles(new: ExtSubmission, current: ExtSubmission) {
        // No need of cleaning remaining files on NFS
    }

    private fun cleanFtpFolder(sub: ExtSubmission) {
        logger.info { "${sub.accNo} ${sub.owner} Started un-publishing files of submission ${sub.accNo} on NFS" }
        FileUtils.deleteFile(folderResolver.getSubmissionFtpFolder(sub.relPath).toFile())
        logger.info { "${sub.accNo} ${sub.owner} Finished un-publishing files of submission ${sub.accNo} on NFS" }
    }

    private fun cleanSubFolder(sub: ExtSubmission) {
        logger.info { "${sub.accNo} ${sub.owner} Started deleting files of submission ${sub.accNo} on NFS" }
        FileUtils.deleteFile(folderResolver.getSubFolder(sub.relPath).toFile())
        logger.info { "${sub.accNo} ${sub.owner} Finished deleting files of submission ${sub.accNo} on NFS" }
    }

    private fun cleanPageTab(sub: ExtSubmission) {
        logger.info { "${sub.accNo} ${sub.owner} Started deleting pagetab files of submission ${sub.accNo} on NFS" }
        sub.allPageTabFiles.filterIsInstance<NfsFile>().forEach { FileUtils.deleteFile(it.file) }
        logger.info { "${sub.accNo} ${sub.owner} Finished deleting pagetab files of submission ${sub.accNo} on NFS" }
    }

    private fun getOrCreateSubmissionFolder(submission: ExtSubmission, permissions: Set<PosixFilePermission>): File {
        val submissionPath = folderResolver.getSubFolder(submission.relPath)
        FileUtils.createParentFolders(submissionPath, RWXR_XR_X)
        return getOrCreateFolder(submissionPath, permissions).toFile()
    }

    private fun getOrCreateTempFolder(
        submissionFolder: File,
        accNo: String,
    ): File = getOrCreateFolder(submissionFolder.parentFile.resolve("${accNo}_temp").toPath(), RWX______).toFile()
}
