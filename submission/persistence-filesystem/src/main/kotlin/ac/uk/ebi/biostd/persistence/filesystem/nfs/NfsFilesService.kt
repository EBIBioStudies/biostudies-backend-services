package ac.uk.ebi.biostd.persistence.filesystem.nfs

import ac.uk.ebi.biostd.persistence.filesystem.api.FilesService
import ac.uk.ebi.biostd.persistence.filesystem.extensions.FilePermissionsExtensions.filePermissions
import ac.uk.ebi.biostd.persistence.filesystem.extensions.FilePermissionsExtensions.folderPermissions
import ac.uk.ebi.biostd.persistence.filesystem.request.FilePersistenceRequest
import ac.uk.ebi.biostd.persistence.filesystem.service.processFiles
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FileMode
import ebi.ac.uk.extended.model.FileMode.COPY
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.FileUtils
import ebi.ac.uk.io.FileUtils.getOrCreateFolder
import ebi.ac.uk.io.FileUtils.reCreateFolder
import ebi.ac.uk.io.RWXR_XR_X
import ebi.ac.uk.io.RWX______
import ebi.ac.uk.paths.FILES_PATH
import ebi.ac.uk.paths.SubmissionFolderResolver
import mu.KotlinLogging
import java.io.File
import java.nio.file.attribute.PosixFilePermission

private val logger = KotlinLogging.logger {}

class NfsFilesService(
    private val folderResolver: SubmissionFolderResolver
) : FilesService {
    override fun persistSubmissionFiles(request: FilePersistenceRequest): ExtSubmission {
        val (submission, mode, _) = request
        logger.info { "Starting processing files of submission ${submission.accNo} over NFS" }

        val filePermissions = submission.filePermissions()
        val folderPermissions = submission.folderPermissions()
        val submissionFolder = getOrCreateSubmissionFolder(submission, folderPermissions)

        val processed = processAttachedFiles(mode, submission, submissionFolder, filePermissions, folderPermissions)
        logger.info { "Finishing processing files of submission ${submission.accNo} over NFS" }

        return processed
    }

    private fun processAttachedFiles(
        mode: FileMode,
        submission: ExtSubmission,
        subFolder: File,
        filePermissions: Set<PosixFilePermission>,
        folderPermissions: Set<PosixFilePermission>
    ): ExtSubmission {
        logger.info { "processing submission ${submission.accNo} files in $mode" }
        val subPath = subFolder.resolve(FILES_PATH)
        val newSubTempPath = createTempFolder(subFolder, submission.accNo)

        val config = NfsFileProcessingConfig(
            mode,
            subFolder = subPath,
            targetFolder = newSubTempPath,
            filePermissions = filePermissions,
            dirPermissions = folderPermissions
        )

        val processed = processFiles(submission) { config.processFile(it) }
        FileUtils.moveFile(newSubTempPath, subPath, filePermissions, folderPermissions)
        logger.info { "Finishing processing submission ${submission.accNo} files in $mode" }
        return processed
    }

    private fun NfsFileProcessingConfig.processFile(file: ExtFile): NfsFile {
        val nfsFile = file as NfsFile
        return if (mode == COPY) nfsCopy(nfsFile) else nfsMove(nfsFile)
    }

    private fun getOrCreateSubmissionFolder(submission: ExtSubmission, permissions: Set<PosixFilePermission>): File {
        val submissionPath = folderResolver.getSubFolder(submission.relPath)
        FileUtils.createParentFolders(submissionPath, RWXR_XR_X)
        return getOrCreateFolder(submissionPath, permissions).toFile()
    }

    private fun createTempFolder(
        submissionFolder: File,
        accNo: String
    ): File = reCreateFolder(submissionFolder.parentFile.resolve("${accNo}_temp"), RWX______)
}
