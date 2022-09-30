package ac.uk.ebi.biostd.persistence.filesystem.nfs

import ac.uk.ebi.biostd.persistence.filesystem.api.FilePersistenceRequest
import ac.uk.ebi.biostd.persistence.filesystem.api.FilesService
import ac.uk.ebi.biostd.persistence.filesystem.api.NfsFilePersistenceRequest
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.extended.model.allPageTabFiles
import ebi.ac.uk.io.FileUtils
import ebi.ac.uk.io.FileUtils.copyOrReplaceFile
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
    override fun cleanSubmissionFile(file: ExtFile) {
        val nfsFile = file as NfsFile
        FileUtils.deleteFile(nfsFile.file)
    }

    override fun persistSubmissionFile(request: FilePersistenceRequest): ExtFile {
        val (file, subFolder, targetFolder, permissions) = request as NfsFilePersistenceRequest
        val target = targetFolder.resolve(file.relPath)
        val subFile = subFolder.resolve(file.relPath)

        if (target.notExist() && subFile.exists() && subFile.md5() == file.md5)
            moveFile(subFile, target, permissions)
        else if (target.notExist())
            copyOrReplaceFile(file.file, target, permissions)

        return file.copy(fullPath = subFile.absolutePath, file = subFile)
    }

    fun cleanSubmissionFiles(sub: ExtSubmission) {
        logger.info { "${sub.accNo} ${sub.owner} Started un-publishing submission files on NFS" }
        FileUtils.deleteFile(folderResolver.getSubmissionFtpFolder(sub.relPath).toFile())
        logger.info { "${sub.accNo} ${sub.owner} Finished un-publishing submission files on NFS" }

        logger.info { "${sub.accNo} ${sub.owner} Started deleting pagetab files on NFS" }
        sub.allPageTabFiles.filterIsInstance<NfsFile>().forEach { cleanSubmissionFile(it) }
        logger.info { "${sub.accNo} ${sub.owner} Finished deleting pagetab files on NFS" }
    }

    fun getOrCreateSubmissionFolder(submission: ExtSubmission, permissions: Set<PosixFilePermission>): File {
        val submissionPath = folderResolver.getSubFolder(submission.relPath)
        FileUtils.createParentFolders(submissionPath, RWXR_XR_X)
        return FileUtils.getOrCreateFolder(submissionPath, permissions).toFile()
    }

    fun createTempSubFolder(
        submissionFolder: File,
        accNo: String,
    ): File = FileUtils.reCreateFolder(submissionFolder.parentFile.resolve("${accNo}_temp"), RWX______)
}
