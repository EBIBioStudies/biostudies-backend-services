package ac.uk.ebi.biostd.persistence.filesystem.nfs

import ac.uk.ebi.biostd.persistence.filesystem.api.FilesService
import ac.uk.ebi.biostd.persistence.filesystem.pagetab.PageTabService
import ac.uk.ebi.biostd.persistence.filesystem.request.FileProcessingConfig
import ac.uk.ebi.biostd.persistence.filesystem.request.FileProcessingRequest
import ac.uk.ebi.biostd.persistence.filesystem.request.PageTabRequest
import ac.uk.ebi.biostd.persistence.filesystem.service.FileProcessingService
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FileMode
import ebi.ac.uk.io.FileUtils
import ebi.ac.uk.io.FileUtils.deleteFile
import ebi.ac.uk.io.FileUtils.getOrCreateFolder
import ebi.ac.uk.io.FileUtils.moveFile
import ebi.ac.uk.io.FileUtils.reCreateFolder
import ebi.ac.uk.io.RWXR_XR_X
import ebi.ac.uk.io.RWXR_X___
import ebi.ac.uk.io.RWX______
import ebi.ac.uk.io.RW_R__R__
import ebi.ac.uk.io.RW_R_____
import ebi.ac.uk.paths.FILES_PATH
import ebi.ac.uk.paths.SubmissionFolderResolver
import mu.KotlinLogging
import java.io.File
import java.nio.file.attribute.PosixFilePermission

private val logger = KotlinLogging.logger {}

class NfsFilesService(
    private val pageTabService: PageTabService,
    private val fileProcessingService: FileProcessingService,
    private val folderResolver: SubmissionFolderResolver
) : FilesService {
    override fun persistSubmissionFiles(submission: ExtSubmission, mode: FileMode): ExtSubmission {
        logger.info { "Starting processing files of submission ${submission.accNo}" }

        val filePermissions = filePermissions(submission.released)
        val folderPermissions = folderPermissions(submission.released)
        val submissionFolder = getOrCreateSubmissionFolder(submission, folderPermissions)

        val processed = processAttachedFiles(mode, submission, submissionFolder, filePermissions, folderPermissions)
        pageTabService.generatePageTab(PageTabRequest(processed, submissionFolder, filePermissions, folderPermissions))
        logger.info { "Finishing processing files of submission ${submission.accNo}" }

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

        val subFilesPath = subFolder.resolve(FILES_PATH)
        val tempFolder = createTempFolder(subFolder, submission.accNo)

        if (subFilesPath.exists()) {
            moveFile(subFilesPath, tempFolder, filePermissions, folderPermissions)
            reCreateFolder(subFolder, folderPermissions)
        }

        val processingConfig = FileProcessingConfig(subFilesPath, tempFolder, filePermissions, folderPermissions)
        val processed = fileProcessingService.processFiles(FileProcessingRequest(mode, submission, processingConfig))

        logger.info { "Finishing processing submission ${submission.accNo} files in $mode" }
        deleteFile(tempFolder)

        return processed
    }

    private fun filePermissions(released: Boolean) = if (released) RW_R__R__ else RW_R_____

    private fun folderPermissions(released: Boolean) = if (released) RWXR_XR_X else RWXR_X___

    private fun getOrCreateSubmissionFolder(submission: ExtSubmission, permissions: Set<PosixFilePermission>): File {
        val submissionPath = folderResolver.getSubFolder(submission.relPath)
        FileUtils.createParentFolders(submissionPath, RWXR_XR_X)
        return getOrCreateFolder(submissionPath, permissions).toFile()
    }

    private fun createTempFolder(submissionFolder: File, accNo: String): File =
        reCreateFolder(submissionFolder.parentFile.resolve("${accNo}_temp"), RWX______)
}
