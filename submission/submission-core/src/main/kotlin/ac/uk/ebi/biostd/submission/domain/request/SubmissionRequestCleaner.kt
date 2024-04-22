package ac.uk.ebi.biostd.submission.domain.request

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.CLEANED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.LOADED
import ac.uk.ebi.biostd.persistence.common.service.RqtUpdate
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestFilesPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.api.FileStorageService
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.StorageMode
import ebi.ac.uk.extended.model.storageMode
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import mu.KotlinLogging
import uk.ac.ebi.events.service.EventsPublisherService
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.filesFlow

private val logger = KotlinLogging.logger {}

class SubmissionRequestCleaner(
    private val storageService: FileStorageService,
    private val serializationService: ExtSerializationService,
    private val eventsPublisherService: EventsPublisherService,
    private val queryService: SubmissionPersistenceQueryService,
    private val requestService: SubmissionRequestPersistenceService,
    private val filesRequestService: SubmissionRequestFilesPersistenceService,
) {
    suspend fun cleanCurrentVersion(
        accNo: String,
        version: Int,
        processId: String,
    ) {
        requestService.onRequest(accNo, version, LOADED, processId, {
            cleanCurrentVersion(it.submission)
            RqtUpdate(it.withNewStatus(CLEANED))
        })
        eventsPublisherService.requestCleaned(accNo, version)
    }

    private suspend fun cleanCurrentVersion(new: ExtSubmission) {
        val current = queryService.findExtByAccNo(new.accNo, includeFileListFiles = true)
        if (current != null) {
            logger.info { "${new.accNo} ${new.owner} Started cleaning common files of version ${new.version}" }
            deleteCommonFiles(new, current)
            logger.info { "${new.accNo} ${new.owner} Finished cleaning common files of version ${new.version}" }
        }
    }

    private suspend fun deleteCommonFiles(
        new: ExtSubmission,
        current: ExtSubmission,
    ) {
        suspend fun deleteFile(
            index: Int,
            file: ExtFile,
        ) {
            logger.info { "${current.accNo} ${current.owner} Deleting file $index, path='${file.filePath}'" }
            storageService.deleteSubmissionFile(current, file)
        }

        logger.info { "${new.accNo} ${current.owner} Building submission files map" }
        val newFiles = summarizeFileRecords(new)
        logger.info { "${new.accNo} ${current.owner} Finished building submission files map" }

        logger.info { "${current.accNo} ${current.owner} Started cleaning common submission files" }
        serializationService.filesFlow(current)
            .filter { newFiles.shouldDelete(it) }
            .collectIndexed { index, file -> deleteFile(index, file) }
        logger.info { "${current.accNo} ${current.owner} Finished cleaning common submission files" }
    }

    private suspend fun summarizeFileRecords(new: ExtSubmission): NewFilesRecords {
        val response = mutableMapOf<String, FileEntry>()
        filesRequestService
            .getSubmissionRequestFiles(new.accNo, new.version, 0)
            .map { it.file }
            .collect { response[it.filePath] = FileEntry(it.md5, new.storageMode) }
        return NewFilesRecords(new.storageMode, response)
    }
}

/**
 * Contains new submission file entries and storage type.
 */
private class NewFilesRecords(
    val storageMode: StorageMode,
    val files: Map<String, FileEntry>,
) {
    /**
     * Validates if there is a entry in the current submission files with the given file Path (storage mode) but
     * diferent Md5.
     */
    fun shouldDelete(existing: ExtFile): Boolean =
        when (val newFile = files[existing.filePath]) {
            null -> false
            else -> newFile.md5 != existing.md5 && storageMode == existing.storageMode
        }
}

private data class FileEntry(val md5: String, val storageMode: StorageMode)
