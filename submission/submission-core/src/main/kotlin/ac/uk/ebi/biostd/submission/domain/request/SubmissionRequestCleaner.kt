package ac.uk.ebi.biostd.submission.domain.request

import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus
import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus.CONFLICTING
import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus.CONFLICTING_PAGE_TAB
import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus.DEPRECATED
import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus.DEPRECATED_PAGE_TAB
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.service.RqtUpdate
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestFilesPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.api.FileStorageService
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.model.RequestStatus.CLEANED
import ebi.ac.uk.model.RequestStatus.PERSISTED
import ebi.ac.uk.model.RequestStatus.PROCESSED
import ebi.ac.uk.model.RequestStatus.VALIDATED
import kotlinx.coroutines.flow.withIndex
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class SubmissionRequestCleaner(
    private val concurrency: Int,
    private val queryService: SubmissionPersistenceQueryService,
    private val storageService: FileStorageService,
    private val requestService: SubmissionRequestPersistenceService,
    private val filesRequestService: SubmissionRequestFilesPersistenceService,
) {
    /**
     *  Executes the clean submission stage where conflicted files (files updated in new submission version) are
     *  deleting to be able to persist new one.
     */
    suspend fun cleanCurrentVersion(
        accNo: String,
        version: Int,
        processId: String,
    ): SubmissionRequest {
        val (rqt) =
            requestService.onRequest(accNo, version, VALIDATED, processId) {
                val previousVersion = it.previousVersion
                if (previousVersion != null) {
                    cleanFiles(accNo, version, previousVersion = previousVersion, CONFLICTING)
                    cleanFiles(accNo, version, previousVersion = previousVersion, CONFLICTING_PAGE_TAB)
                }
                RqtUpdate(it.withNewStatus(CLEANED))
            }
        return rqt
    }

    /**
     * Executes the finalize or submission processing stage when files deprecated (file not used anymore) from previous
     * version are deleted. Note that submission is query wth negative version as new version has been already
     * persisted at this point.
     */
    suspend fun finalizeRequest(
        accNo: String,
        version: Int,
        processId: String,
    ) {
        requestService.onRequest(accNo, version, PERSISTED, processId) {
            val previousVersion = it.previousVersion
            if (previousVersion != null) {
                cleanFiles(accNo, version, previousVersion = -previousVersion, DEPRECATED)
                cleanFiles(accNo, version, previousVersion = -previousVersion, DEPRECATED_PAGE_TAB)
            }

            RqtUpdate(it.withNewStatus(PROCESSED))
        }
    }

    private suspend fun cleanFiles(
        accNo: String,
        version: Int,
        previousVersion: Int,
        status: RequestFileStatus,
    ) {
        val sub = queryService.getCoreInfoByAccNoAndVersion(accNo, previousVersion)

        suspend fun deleteFile(
            index: Int,
            file: ExtFile,
        ) {
            logger.info { "${sub.accNo} ${sub.owner} Deleting file $index, path='${file.filePath}'" }
            storageService.deleteSubmissionFile(sub, file)
        }

        logger.info { "${sub.accNo} ${sub.owner} Started cleaning submission files, concurrency: '$concurrency'" }
        filesRequestService
            .getSubmissionRequestFiles(accNo, version, status)
            .withIndex()
            .collect { (idx, file) ->
                deleteFile(idx, file.file)
                requestService.updateRqtFile(file.copy(status = RequestFileStatus.CLEANED))
            }
        logger.info { "${sub.accNo} ${sub.owner} Finished cleaning submission files, concurrency: '$concurrency'" }
    }
}
