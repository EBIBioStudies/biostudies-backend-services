package ac.uk.ebi.biostd.submission.domain.request

import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus
import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus.CONFLICTING
import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus.CONFLICTING_PAGE_TAB
import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus.DEPRECATED
import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus.DEPRECATED_PAGE_TAB
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestFile
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestFilesPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.api.FileStorageService
import ebi.ac.uk.coroutines.chunked
import ebi.ac.uk.coroutines.concurrently
import ebi.ac.uk.model.RequestStatus.CLEANED
import ebi.ac.uk.model.RequestStatus.PERSISTED
import ebi.ac.uk.model.RequestStatus.PROCESSED
import ebi.ac.uk.model.RequestStatus.VALIDATED
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
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
     *  Executes the clean submission stage where conflicted files (files updated in a new submission version) are
     *  deleted to be able to persist the new one.
     */
    suspend fun cleanCurrentVersion(
        accNo: String,
        version: Int,
        processId: String,
    ): SubmissionRequest =
        requestService.onRequest(accNo, version, VALIDATED, processId) {
            val previousVersion = it.process!!.previousVersion
            if (previousVersion != null) {
                cleanFiles(accNo, version, previousVersion = previousVersion, CONFLICTING)
                cleanFiles(accNo, version, previousVersion = previousVersion, CONFLICTING_PAGE_TAB)
            }
            it.withNewStatus(CLEANED)
        }

    /**
     * Executes the clean previous version stage where deprecated files from the previous version (files that are no
     * longer used) are deleted. Note that submission is query wth negative version as new version has been already
     * persisted at this point.
     */
    suspend fun cleanPreviousVersion(
        accNo: String,
        version: Int,
        processId: String,
    ): SubmissionRequest =
        requestService.onRequest(accNo, version, PERSISTED, processId) {
            val previousVersion = it.process!!.previousVersion
            if (previousVersion != null) {
                cleanFiles(accNo, version, previousVersion = -previousVersion, DEPRECATED)
                cleanFiles(accNo, version, previousVersion = -previousVersion, DEPRECATED_PAGE_TAB)
            }

            it.withNewStatus(PROCESSED)
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
            rqtFile: SubmissionRequestFile,
        ): SubmissionRequestFile {
            val file = rqtFile.file
            logger.info { "${sub.accNo} ${sub.owner} Deleting file $index, path='${file.filePath}'" }
            storageService.deleteSubmissionFile(sub, file)
            return rqtFile
        }

        logger.info { "${sub.accNo} ${sub.owner} Started cleaning submission files, concurrency: '$concurrency'" }
        filesRequestService
            .getSubmissionRequestFiles(accNo, version, status)
            .withIndex()
            .concurrently(concurrency) { deleteFile(it.index, it.value) }
            .chunked(concurrency)
            .onEach { requestService.updateRqtFiles(it) }
            .collect()
        logger.info { "${sub.accNo} ${sub.owner} Finished cleaning submission files, concurrency: '$concurrency'" }
    }
}
