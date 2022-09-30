package ac.uk.ebi.biostd.persistence.filesystem.service

import ac.uk.ebi.biostd.persistence.filesystem.api.FileStorageService
import ac.uk.ebi.biostd.persistence.filesystem.api.NfsFilePersistenceRequest
import ac.uk.ebi.biostd.persistence.filesystem.extensions.FilePermissionsExtensions.permissions
import ac.uk.ebi.biostd.persistence.filesystem.nfs.NfsFilesService
import ac.uk.ebi.biostd.persistence.filesystem.nfs.NfsFtpService
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.extended.model.allPageTabFiles
import ebi.ac.uk.io.FileUtils
import ebi.ac.uk.io.FileUtils.moveFile
import ebi.ac.uk.io.Permissions
import ebi.ac.uk.io.RWXR_XR_X
import ebi.ac.uk.io.RWX______
import ebi.ac.uk.paths.SubmissionFolderResolver
import mu.KotlinLogging
import uk.ac.ebi.extended.serialization.service.FileProcessingService
import java.io.File
import java.nio.file.attribute.PosixFilePermission

private val logger = KotlinLogging.logger {}

// TODO not needed
class NfsStorageService(
    private val ftpService: NfsFtpService,
    private val filesService: NfsFilesService,
    private val folderResolver: SubmissionFolderResolver,
    private val fileProcessingService: FileProcessingService,
) : FileStorageService {
    override fun cleanSubmissionFiles(sub: ExtSubmission) {
//        logger.info { "${sub.accNo} ${sub.owner} Started un-publishing submission files on NFS" }
//        FileUtils.deleteFile(folderResolver.getSubmissionFtpFolder(sub.relPath).toFile())
//        logger.info { "${sub.accNo} ${sub.owner} Finished un-publishing submission files on NFS" }
//
//        logger.info { "${sub.accNo} ${sub.owner} Started deleting pagetab files on NFS" }
//        sub.allPageTabFiles.filterIsInstance<NfsFile>().forEach { filesService.cleanSubmissionFile(it) }
//        logger.info { "${sub.accNo} ${sub.owner} Finished deleting pagetab files on NFS" }
        TODO()
    }

    override fun persistSubmissionFiles(sub: ExtSubmission): ExtSubmission {
//        val subFolder = getOrCreateSubmissionFolder(sub, sub.permissions().folder)
//
//        logger.info { "${sub.accNo} ${sub.owner} Started persisting submission files on NFS" }
//        val processed = processAttachedFiles(sub, subFolder, createTempFolder(subFolder, sub.accNo), sub.permissions())
//        logger.info { "${sub.accNo} ${sub.owner} Finished persisting submission files on NFS" }
//
//        return processed
        TODO()
    }

//    private fun processAttachedFiles(
//        sub: ExtSubmission,
//        subFolder: File,
//        targetFolder: File,
//        permissions: Permissions,
//    ): ExtSubmission {
//        val processed = fileProcessingService.processFiles(sub) { file, _ ->
//            logger.info { "${sub.accNo} ${sub.owner} Copying file $file with size ${file.size} into $targetFolder" }
//            val request = NfsFilePersistenceRequest(file as NfsFile, subFolder, targetFolder, permissions)
//            filesService.persistSubmissionFile(request)
//        }
//        moveFile(targetFolder, subFolder, permissions)
//
//        return processed
//    }
//
//    private fun getOrCreateSubmissionFolder(submission: ExtSubmission, permissions: Set<PosixFilePermission>): File {
//        val submissionPath = folderResolver.getSubFolder(submission.relPath)
//        FileUtils.createParentFolders(submissionPath, RWXR_XR_X)
//        return FileUtils.getOrCreateFolder(submissionPath, permissions).toFile()
//    }
//
//    private fun createTempFolder(
//        submissionFolder: File,
//        accNo: String,
//    ): File = FileUtils.reCreateFolder(submissionFolder.parentFile.resolve("${accNo}_temp"), RWX______)

    override fun releaseSubmissionFiles(sub: ExtSubmission) {
//        ftpService.releaseSubmissionFiles(sub)
    }
}
