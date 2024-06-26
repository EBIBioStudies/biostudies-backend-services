package ac.uk.ebi.biostd.submission.domain.request

import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus.CONFLICTING
import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus.DEPRECATED
import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus.LOADED
import ac.uk.ebi.biostd.persistence.common.service.RqtUpdate
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestFilesPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.StorageMode
import ebi.ac.uk.extended.model.storageMode
import ebi.ac.uk.model.RequestStatus
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import mu.KotlinLogging
import uk.ac.ebi.events.service.EventsPublisherService
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.filesFlow
import java.util.concurrent.atomic.AtomicInteger
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestFile as SubRqtFile

private val logger = KotlinLogging.logger {}

class SubmissionRequestCleanIndexer(
    private val serializationService: ExtSerializationService,
    private val queryService: SubmissionPersistenceQueryService,
    private val filesRequestService: SubmissionRequestFilesPersistenceService,
    private val requestService: SubmissionRequestPersistenceService,
    private val eventsPublisherService: EventsPublisherService,
) {
    /**
     * Index submission request to clean files by creating records for each one.
     */
    suspend fun indexRequest(
        accNo: String,
        version: Int,
        processId: String,
    ) {
        requestService.onRequest(accNo, version, RequestStatus.LOADED, processId) {
            val (activeVersion, conflicted, deprecated) = indexRequest(it.submission)
            RqtUpdate(it.cleanIndexed(conflicted, deprecated, activeVersion))
        }
        eventsPublisherService.requestIndexedToClean(accNo, version)
    }

    suspend fun indexRequest(new: ExtSubmission): Triple<Int?, Int, Int> {
        val current = queryService.findExtByAccNo(new.accNo, includeFileListFiles = true)
        if (current != null) {
            logger.info { "${new.accNo} ${new.owner} Started indexing submission files to be cleaned" }
            val newFiles = summarizeFileRecords(new)
            val response = indexToCleanFiles(new = new, newFiles = newFiles, current = current)
            logger.info { "${new.accNo} ${new.owner} Finished indexing submission files to be cleaned" }
            return response
        }

        return Triple(null, 0, 0)
    }

    private suspend fun indexToCleanFiles(
        new: ExtSubmission,
        newFiles: FilesRecords,
        current: ExtSubmission,
    ): Triple<Int, Int, Int> {
        val conflictIdx = AtomicInteger(0)
        val deprecatedIdx = AtomicInteger(0)

        serializationService.filesFlow(current)
            .mapNotNull { file ->
                when (newFiles.findMatch(file)) {
                    MatchType.CONFLICTING -> SubRqtFile(new, conflictIdx.incrementAndGet(), file, CONFLICTING, true)
                    MatchType.DEPRECATED -> SubRqtFile(new, deprecatedIdx.incrementAndGet(), file, DEPRECATED, true)
                    MatchType.REUSED -> null
                }
            }
            .collect {
                logger.info { "${new.accNo} ${new.owner} Indexing to clean file ${it.index}, path='${it.path}'" }
                filesRequestService.saveSubmissionRequestFile(it)
            }
        return Triple(current.version, conflictIdx.get(), deprecatedIdx.get())
    }

    private suspend fun summarizeFileRecords(new: ExtSubmission): FilesRecords {
        val response = mutableMapOf<String, FileRecord>()
        filesRequestService
            .getSubmissionRequestFiles(new.accNo, new.version, LOADED)
            .map { it.file }
            .collect { response[it.filePath] = FileRecord(it.md5, new.storageMode) }
        return FilesRecords(new.storageMode, response)
    }
}

/**
 * Contains new submission file entries and storage type.
 */
private class FilesRecords(
    val storageMode: StorageMode,
    val files: Map<String, FileRecord>,
) {
    /**
     * Validates if there is a entry in the current submission files with the given file Path (storage mode) but
     * diferent Md5.
     */
    fun findMatch(existing: ExtFile): MatchType {
        val newFile = files[existing.filePath]
        return when {
            newFile == null || newFile.storageMode != existing.storageMode -> MatchType.DEPRECATED
            newFile.md5 != existing.md5 -> MatchType.CONFLICTING
            else -> MatchType.REUSED
        }
    }
}

private enum class MatchType {
    CONFLICTING,
    DEPRECATED,
    REUSED,
}

private data class FileRecord(val md5: String, val storageMode: StorageMode)
