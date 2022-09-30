package ac.uk.ebi.biostd.submission.submitter.request

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.FILES_COPIED
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.api.FireFilePersistenceRequest
import ac.uk.ebi.biostd.persistence.filesystem.api.NfsFilePersistenceRequest
import ac.uk.ebi.biostd.persistence.filesystem.extensions.FilePermissionsExtensions.permissions
import ac.uk.ebi.biostd.persistence.filesystem.fire.FireFilesService
import ac.uk.ebi.biostd.persistence.filesystem.nfs.NfsFilesService
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.extended.model.StorageMode.FIRE
import ebi.ac.uk.extended.model.StorageMode.NFS
import ebi.ac.uk.io.FileUtils
import mu.KotlinLogging
import uk.ac.ebi.extended.serialization.service.FileProcessingService

private val logger = KotlinLogging.logger {}

class SubmissionRequestProcessor(
    private val nfsFilesService: NfsFilesService,
    private val fireFilesService: FireFilesService,
    private val fileProcessingService: FileProcessingService,
    private val queryService: SubmissionPersistenceQueryService,
    private val persistenceService: SubmissionPersistenceService,
) {
    /**
     * Process the current submission files. Note that [ExtSubmission] returned does not include file list files.
     */
    fun processRequest(accNo: String, version: Int): ExtSubmission {
        val request = queryService.getCleanedRequest(accNo, version)
        val (sub, _) = request

        logger.info { "$accNo ${sub.owner} Started copying files accNo='${sub.accNo}', version='$version'" }

        val processed = persistSubmissionFiles(sub)
        persistenceService.expirePreviousVersions(sub.accNo)
        persistenceService.saveSubmission(processed)
        val processedRequest = request.copy(status = FILES_COPIED, submission = processed, currentIndex = 0)
        persistenceService.saveSubmissionRequest(processedRequest)

        logger.info { "$accNo ${sub.owner} Finished copying files accNo='$accNo', version='$version'" }

        return processed
    }

    private fun persistSubmissionFiles(sub: ExtSubmission) = when (sub.storageMode) {
        NFS -> persistNfsFiles(sub)
        FIRE -> persistFireFiles(sub)
    }

    private fun persistFireFiles(sub: ExtSubmission): ExtSubmission {
        logger.info { "${sub.accNo} ${sub.owner} Started persisting submission files on FIRE" }

        val persisted = fileProcessingService.processFiles(sub) { file, idx ->
            logger.info { "${sub.accNo} ${sub.owner} Started persisting file $idx, path='${file.filePath}' on FIRE" }

            val request = FireFilePersistenceRequest(sub.accNo, sub.version, sub.relPath, file)
            val fireFile = fireFilesService.persistSubmissionFile(request)
            persistenceService.updateRequestIndex(sub.accNo, sub.version, idx)

            logger.info { "${sub.accNo} ${sub.owner} Finished persisting file $idx, path='${file.filePath}' on FIRE" }

            fireFile
        }

        logger.info { "${sub.accNo} ${sub.owner} Finished persisting submission files on FIRE" }

        return persisted
    }

    private fun persistNfsFiles(sub: ExtSubmission): ExtSubmission {
        logger.info { "${sub.accNo} ${sub.owner} Started persisting submission files on NFS" }

        val permissions = sub.permissions()
        val subFolder = nfsFilesService.getOrCreateSubmissionFolder(sub, permissions.folder)
        val targetFolder = nfsFilesService.createTempSubFolder(subFolder, sub.accNo)

        val persisted = fileProcessingService.processFiles(sub) { file, idx ->
            logger.info { "${sub.accNo}, ${sub.owner} Started persisting file $idx, path='${file.filePath}' on NFS" }

            val request = NfsFilePersistenceRequest(file as NfsFile, subFolder, targetFolder, permissions)
            val nfsFile = nfsFilesService.persistSubmissionFile(request)
            persistenceService.updateRequestIndex(sub.accNo, sub.version, idx)

            logger.info { "${sub.accNo}, ${sub.owner} Finished persisting file $idx, path='${file.filePath}' on NFS" }

            nfsFile
        }

        FileUtils.moveFile(targetFolder, subFolder, permissions)

        logger.info { "${sub.accNo} ${sub.owner} Finished persisting submission files on NFS" }

        return persisted
    }
}
