package ac.uk.ebi.biostd.submission.submitter.request

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.FILES_COPIED
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestFile
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestFilesPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.api.FileStorageService
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import mu.KotlinLogging
import uk.ac.ebi.extended.serialization.service.FileProcessingService
import java.time.OffsetDateTime

private val logger = KotlinLogging.logger {}

class SubmissionRequestProcessor(
    private val storageService: FileStorageService,
    private val fileProcessingService: FileProcessingService,
    private val persistenceService: SubmissionPersistenceService,
    private val requestService: SubmissionRequestPersistenceService,
    private val filesRequestService: SubmissionRequestFilesPersistenceService,
) {
    /**
     * Process the current submission files. Note that [ExtSubmission] returned does not include file list files.
     */
    fun processRequest(accNo: String, version: Int): ExtSubmission {
        val request = requestService.getCleanedRequest(accNo, version)
        val sub = request.submission

        logger.info { "$accNo ${sub.owner} Started persisting submission files on ${sub.storageMode}" }

        persistSubmissionFiles(sub, request.currentIndex)
        val processed = assembleSubmission(sub)
        storageService.postProcessSubmissionFiles(sub)
        persistenceService.expirePreviousVersions(sub.accNo)
        persistenceService.saveSubmission(processed)

        val processedRequest = request.copy(
            status = FILES_COPIED,
            submission = processed,
            currentIndex = 0,
            modificationTime = OffsetDateTime.now(),
        )
        requestService.saveSubmissionRequest(processedRequest)

        logger.info { "$accNo ${sub.owner} Finished persisting submission files on ${sub.storageMode}" }

        return processed
    }

    private fun persistSubmissionFiles(sub: ExtSubmission, startingAt: Int) {
        fun persistSubmissionFile(file: ExtFile, idx: Int) {
            logger.info { "${sub.accNo} ${sub.owner} Started persisting file $idx, path='${file.filePath}'" }

            val persisted = storageService.persistSubmissionFile(sub, file)
            val updatedFile = SubmissionRequestFile(sub.accNo, sub.version, idx, file.filePath, persisted)
            filesRequestService.upsertSubmissionRequestFile(updatedFile)
            requestService.updateRequestIndex(sub.accNo, sub.version, idx)

            logger.info { "${sub.accNo} ${sub.owner} Finished persisting file $idx, path='${file.filePath}'" }
        }

        filesRequestService
            .getSubmissionRequestFiles(sub.accNo, sub.version, startingAt)
            .forEach { persistSubmissionFile(it.file, it.index) }
    }

    private fun assembleSubmission(sub: ExtSubmission) =
        fileProcessingService.processFiles(sub) { file, _ ->
            filesRequestService.getSubmissionRequestFile(sub.accNo, sub.version, file.filePath).file
        }
}
