package ac.uk.ebi.biostd.submission.submitter.request

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.FILES_COPIED
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.api.FilePersistenceConfig
import ac.uk.ebi.biostd.persistence.filesystem.api.FileStorageService
import ebi.ac.uk.extended.model.ExtSubmission
import mu.KotlinLogging
import uk.ac.ebi.extended.serialization.service.FileProcessingService

private val logger = KotlinLogging.logger {}

class SubmissionRequestProcessor(
    private val storageService: FileStorageService,
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

        logger.info { "$accNo ${sub.owner} Started persisting submission files on ${sub.storageMode}" }

        val filePersistenceConfig = storageService.preProcessSubmissionFiles(sub)
        val processed = persistSubmissionFiles(sub, filePersistenceConfig)

        storageService.postProcessSubmissionFiles(filePersistenceConfig)
        persistenceService.expirePreviousVersions(sub.accNo)
        persistenceService.saveSubmission(processed)
        persistenceService.saveSubmissionRequest(request.copy(status = FILES_COPIED, submission = processed))

        logger.info { "$accNo ${sub.owner} Finished persisting submission files on ${sub.storageMode}" }

        return processed
    }

    private fun persistSubmissionFiles(sub: ExtSubmission, config: FilePersistenceConfig) =
        fileProcessingService.processFiles(sub) { file, idx ->
            logger.info { "${sub.accNo} ${sub.owner} Persisting file $idx, path='${file.filePath}'" }
            storageService.persistSubmissionFile(file, config)
        }
}
