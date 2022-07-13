package ac.uk.ebi.biostd.persistence.filesystem.nfs

import ac.uk.ebi.biostd.persistence.filesystem.api.FilesService
import ac.uk.ebi.biostd.persistence.filesystem.extensions.FilePermissionsExtensions.permissions
import ac.uk.ebi.biostd.persistence.filesystem.request.FilePersistenceRequest
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FileMode
import ebi.ac.uk.extended.model.FileMode.COPY
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
    override fun persistSubmissionFiles(rqt: FilePersistenceRequest): ExtSubmission {
        val (sub, mode) = rqt
        logger.info { "${sub.accNo} ${sub.owner} Processing files of submission ${sub.accNo} over NFS" }

        val submissionFolder = getOrCreateSubmissionFolder(sub, sub.permissions().folder)

        val processed = processAttachedFiles(mode, sub, submissionFolder, sub.permissions())
        logger.info { "${sub.accNo} ${sub.owner} Finished processing files of submission ${sub.accNo} over NFS" }

        return processed
    }

    override fun cleanSubmissionFiles(sub: ExtSubmission) {
        logger.info { "${sub.accNo} ${sub.owner} Un-publishing files of submission ${sub.accNo} over NFS" }

        FileUtils.deleteFile(folderResolver.getSubmissionFtpFolder(sub.relPath).toFile())

        logger.info { "${sub.accNo} ${sub.owner} Finished un-publishing files of submission ${sub.accNo} over NFS" }
    }

    private fun processAttachedFiles(
        mode: FileMode,
        sub: ExtSubmission,
        subFolder: File,
        permissions: Permissions,
    ): ExtSubmission {
        val newSubTempPath = createTempFolder(subFolder, sub.accNo)

        val config = NfsFileProcessingConfig(
            sub.accNo,
            sub.owner,
            mode,
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
        accNo: String,
    ): File = reCreateFolder(submissionFolder.parentFile.resolve("${accNo}_temp"), RWX______)
}
