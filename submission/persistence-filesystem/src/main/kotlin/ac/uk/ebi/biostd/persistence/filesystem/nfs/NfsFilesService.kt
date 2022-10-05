package ac.uk.ebi.biostd.persistence.filesystem.nfs

import ac.uk.ebi.biostd.persistence.filesystem.api.FilePersistenceConfig
import ac.uk.ebi.biostd.persistence.filesystem.api.FilesService
import ac.uk.ebi.biostd.persistence.filesystem.api.NfsFilePersistenceConfig
import ac.uk.ebi.biostd.persistence.filesystem.extensions.FilePermissionsExtensions.permissions
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.extended.model.allPageTabFiles
import ebi.ac.uk.io.FileUtils
import ebi.ac.uk.io.FileUtils.copyOrReplaceFile
import ebi.ac.uk.io.FileUtils.getOrCreateFolder
import ebi.ac.uk.io.FileUtils.moveFile
import ebi.ac.uk.io.FileUtils.reCreateFolder
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
    override fun preProcessSubmissionFiles(sub: ExtSubmission): FilePersistenceConfig {
        val permissions = sub.permissions()
        val subFolder = getOrCreateSubmissionFolder(sub, permissions.folder)
        val targetFolder = createTempFolder(subFolder, sub.accNo)

        return NfsFilePersistenceConfig(subFolder, targetFolder, permissions)
    }

    override fun persistSubmissionFile(file: ExtFile, config: FilePersistenceConfig): ExtFile {
        val (subFolder, targetFolder, permissions) = config as NfsFilePersistenceConfig
        val extFile = file as NfsFile
        val target = targetFolder.resolve(extFile.relPath)
        val subFile = subFolder.resolve(extFile.relPath)

        if (target.notExist() && subFile.exists() && subFile.md5() == extFile.md5)
            moveFile(subFile, target, permissions)
        else if (target.notExist())
            copyOrReplaceFile(extFile.file, target, permissions)

        return extFile.copy(fullPath = subFile.absolutePath, file = subFile)
    }

    override fun postProcessSubmissionFiles(config: FilePersistenceConfig) {
        val (subFolder, targetFolder, permissions) = config as NfsFilePersistenceConfig
        moveFile(targetFolder, subFolder, permissions)
    }

    override fun cleanSubmissionFiles(sub: ExtSubmission) {
        logger.info { "${sub.accNo} ${sub.owner} Un-publishing files of submission ${sub.accNo} on NFS" }
        FileUtils.deleteFile(folderResolver.getSubmissionFtpFolder(sub.relPath).toFile())
        logger.info { "${sub.accNo} ${sub.owner} Finished un-publishing files of submission ${sub.accNo} on NFS" }

        logger.info { "${sub.accNo} ${sub.owner} Deleting pagetab files of submission ${sub.accNo} on NFS" }
        sub.allPageTabFiles.filterIsInstance<NfsFile>().forEach { FileUtils.deleteFile(it.file) }
        logger.info { "${sub.accNo} ${sub.owner} Finished deleting pagetab files of submission ${sub.accNo} on NFS" }
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
