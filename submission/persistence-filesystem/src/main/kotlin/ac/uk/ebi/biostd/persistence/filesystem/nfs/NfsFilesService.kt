package ac.uk.ebi.biostd.persistence.filesystem.nfs

import ac.uk.ebi.biostd.persistence.filesystem.api.FilesService
import ac.uk.ebi.biostd.persistence.filesystem.extensions.FilePermissionsExtensions.permissions
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.extended.model.allPageTabFiles
import ebi.ac.uk.io.FileUtils
import ebi.ac.uk.io.FileUtils.copyOrReplaceFile
import ebi.ac.uk.io.FileUtils.getOrCreateFolder
import ebi.ac.uk.io.FileUtils.moveFile
import ebi.ac.uk.io.FileUtils.reCreateFolder
import ebi.ac.uk.io.Permissions
import ebi.ac.uk.io.RWXR_XR_X
import ebi.ac.uk.io.RWX______
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.notExist
import ebi.ac.uk.paths.SubmissionFolderResolver
import mu.KotlinLogging
import uk.ac.ebi.extended.serialization.service.FileProcessingService
import java.io.File
import java.nio.file.attribute.PosixFilePermission

private val logger = KotlinLogging.logger {}

class NfsFilesService(
    private val folderResolver: SubmissionFolderResolver,
    private val processingService: FileProcessingService,
) : FilesService {
    override fun persistSubmissionFiles(sub: ExtSubmission): ExtSubmission {
        val subFolder = getOrCreateSubmissionFolder(sub, sub.permissions().folder)

        logger.info { "${sub.accNo} ${sub.owner} Processing files of submission ${sub.accNo} on NFS" }
        val processed = processAttachedFiles(sub, subFolder, createTempFolder(subFolder, sub.accNo), sub.permissions())
        logger.info { "${sub.accNo} ${sub.owner} Finished processing files of submission ${sub.accNo} on NFS" }
        return processed
    }

    override fun cleanSubmissionFiles(sub: ExtSubmission) {
        logger.info { "${sub.accNo} ${sub.owner} Un-publishing files of submission ${sub.accNo} on NFS" }
        FileUtils.deleteFile(folderResolver.getSubmissionFtpFolder(sub.relPath).toFile())
        logger.info { "${sub.accNo} ${sub.owner} Finished un-publishing files of submission ${sub.accNo} on NFS" }

        logger.info { "${sub.accNo} ${sub.owner} Deleting pagetab files of submission ${sub.accNo} on NFS" }
        sub.allPageTabFiles.filterIsInstance<NfsFile>().forEach { FileUtils.deleteFile(it.file) }
        logger.info { "${sub.accNo} ${sub.owner} Finished deleting pagetab files of submission ${sub.accNo} on NFS" }
    }

    private fun processAttachedFiles(
        sub: ExtSubmission,
        subFolder: File,
        targetFolder: File,
        permissions: Permissions,
    ): ExtSubmission {
        fun copyFile(extFile: NfsFile, idx: Int): NfsFile {
            val file = extFile.file
            val target = targetFolder.resolve(extFile.relPath)
            val subFile = subFolder.resolve(extFile.relPath)

            logger.info { "${sub.accNo} ${sub.owner} Copying file $idx, $file with size ${extFile.size} into $target" }

            if (target.notExist() && subFile.exists() && subFile.md5() == extFile.md5)
                moveFile(subFile, target, permissions)
            else if (target.notExist())
                copyOrReplaceFile(file, target, permissions)

            return extFile.copy(fullPath = subFile.absolutePath, file = subFile)
        }

        val result = processingService.processFiles(sub) { file, idx -> copyFile(file as NfsFile, idx) }
        moveFile(targetFolder, subFolder, permissions)
        return result
    }

    private fun getOrCreateSubmissionFolder(submission: ExtSubmission, permissions: Set<PosixFilePermission>): File {
        val submissionPath = folderResolver.getSubFolder(submission.relPath)
        FileUtils.createParentFolders(submissionPath, RWXR_XR_X)
        return getOrCreateFolder(submissionPath, permissions).toFile()
    }

    private fun createTempFolder(
        submissionFolder: File,
        accNo: String,
    ): File = reCreateFolder(submissionFolder.parentFile.resolve("${accNo}_temp"), RWX______)
}
