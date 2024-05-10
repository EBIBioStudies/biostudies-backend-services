package ac.uk.ebi.biostd.submission.domain.request

import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus.CONFLICTING
import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus.DEPRECATED
import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus.INDEXED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus
import ac.uk.ebi.biostd.persistence.common.service.RqtUpdate
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestFilesPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.StorageMode
import ebi.ac.uk.extended.model.storageMode
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
            val (conflicted, deprecated) = indexRequest(it.submission)
            RqtUpdate(it.cleanIndexed(conflictedFiles = conflicted, deprecatedFiles = deprecated))
        }
        eventsPublisherService.requestIndexedToClean(accNo, version)
    }

    suspend fun indexRequest(new: ExtSubmission): Pair<Int, Int> {
        val current = queryService.findExtByAccNo(new.accNo, includeFileListFiles = true)
        if (current != null) {
            logger.info { "${new.accNo} ${new.owner} Started indexing submission files to be cleaned" }
            val newFiles = summarizeFileRecords(new)
            val response = indexToCleanFiles(newFiles, current)
            logger.info { "${new.accNo} ${new.owner} Finished indexing submission files to be cleaned" }
            return response
        }

        return 0 to 0
    }

    private suspend fun indexToCleanFiles(
        newFiles: FilesRecords,
        current: ExtSubmission,
    ): Pair<Int, Int> {
        val conflictIdx = AtomicInteger(0)
        val deprecatedIdx = AtomicInteger(0)

        serializationService.filesFlow(current)
            .mapNotNull { file ->
                when (newFiles.findMatch(file)) {
                    MatchType.CONFLICTING -> SubRqtFile(current, conflictIdx.incrementAndGet(), file, CONFLICTING)
                    MatchType.DEPRECATED -> SubRqtFile(current, deprecatedIdx.incrementAndGet(), file, DEPRECATED)
                    MatchType.REUSED -> null
                }
            }
            .collect {
                logger.info { "${current.accNo} ${current.owner} Indexing to clean file ${it.index}, path='${it.path}'" }
                filesRequestService.saveSubmissionRequestFile(it)
            }
        return conflictIdx.get() to deprecatedIdx.get()
    }

    private suspend fun summarizeFileRecords(new: ExtSubmission): FilesRecords {
        val response = mutableMapOf<String, FileRecord>()
        filesRequestService
            .getSubmissionRequestFiles(new.accNo, new.version, INDEXED)
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
            newFile == null -> MatchType.DEPRECATED
            newFile.md5 != existing.md5 && storageMode == existing.storageMode -> MatchType.CONFLICTING
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
