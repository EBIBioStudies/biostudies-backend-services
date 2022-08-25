package ac.uk.ebi.biostd.persistence.filesystem.nfs

import ac.uk.ebi.biostd.persistence.filesystem.api.FilesService
import ac.uk.ebi.biostd.persistence.filesystem.extensions.FilePermissionsExtensions.permissions
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.FileUtils
import ebi.ac.uk.io.FileUtils.getOrCreateFolder
import ebi.ac.uk.io.FileUtils.moveFile
import ebi.ac.uk.io.FileUtils.reCreateFolder
import ebi.ac.uk.io.Permissions
import ebi.ac.uk.io.RWXR_XR_X
import ebi.ac.uk.io.RWX______
import ebi.ac.uk.paths.FILES_PATH
import ebi.ac.uk.paths.SubmissionFolderResolver
import mu.KotlinLogging
import uk.ac.ebi.extended.serialization.service.FileProcessingService
import java.io.File
import java.nio.file.attribute.PosixFilePermission

private val logger = KotlinLogging.logger {}

class NfsFilesService(
    private val folderResolver: SubmissionFolderResolver,
    private val fileProcessingService: FileProcessingService,
) : FilesService {
    override fun persistSubmissionFiles(sub: ExtSubmission): ExtSubmission {
        logger.info { "${sub.accNo} ${sub.owner} Processing files of submission ${sub.accNo} on NFS" }

        val submissionFolder = getOrCreateSubmissionFolder(sub, sub.permissions().folder)

        val processed = processAttachedFiles(sub, submissionFolder, sub.permissions())
        logger.info { "${sub.accNo} ${sub.owner} Finished processing files of submission ${sub.accNo} on NFS" }

        return processed
    }

    override fun cleanSubmissionFiles(sub: ExtSubmission) {
        logger.info { "${sub.accNo} ${sub.owner} Un-publishing files of submission ${sub.accNo} on NFS" }

        FileUtils.deleteFile(folderResolver.getSubmissionFtpFolder(sub.relPath).toFile())

        logger.info { "${sub.accNo} ${sub.owner} Finished un-publishing files of submission ${sub.accNo} on NFS" }
    }

    private fun processAttachedFiles(
        sub: ExtSubmission,
        subFolder: File,
        permissions: Permissions,
    ): ExtSubmission {
        val newSubTempPath = createTempFolder(subFolder, sub.accNo)

        val config = NfsFileProcessingConfig(
            sub.accNo,
            sub.owner,
            subFolder = subFolder.resolve(FILES_PATH),
            targetFolder = newSubTempPath.resolve(FILES_PATH),
            permissions = permissions
        )

        val processed = fileProcessingService.processFiles(sub) { file, index ->
            logger.debug { "${sub.accNo}, ${sub.version} Processing file $index, path='${file.filePath}'" }
            config.processFile(file)
        }
        moveFile(newSubTempPath, subFolder, permissions)
        return processed
    }

    private fun NfsFileProcessingConfig.processFile(file: ExtFile): NfsFile = nfsCopy(file as NfsFile)

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
