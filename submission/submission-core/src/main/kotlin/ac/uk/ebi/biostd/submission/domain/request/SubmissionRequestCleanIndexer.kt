package ac.uk.ebi.biostd.submission.domain.request

import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus
import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus.INDEXED
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestFile
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestFilesPersistenceService
import ac.uk.ebi.biostd.submission.domain.request.MatchType.CONFLICTING
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.StorageMode
import ebi.ac.uk.extended.model.storageMode
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.withIndex
import mu.KotlinLogging
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.filesFlow
import java.util.concurrent.atomic.AtomicInteger

private val logger = KotlinLogging.logger {}

class SubmissionRequestCleanIndexer(
    private val serializationService: ExtSerializationService,
    private val queryService: SubmissionPersistenceQueryService,
    private val filesRequestService: SubmissionRequestFilesPersistenceService,
) {
    suspend fun indexRequest(new: ExtSubmission): Int {
        val current = queryService.findExtByAccNo(new.accNo, includeFileListFiles = true)
        if (current != null) {
            logger.info { "${new.accNo} ${new.owner} Started indexing submission files to be cleaned" }
            val newFiles = summarizeFileRecords(new)
            val totalFiles = indexToCleanFiles(newFiles, current)
            logger.info { "${new.accNo} ${new.owner} Finished indexing submission files to be cleaned" }
            return totalFiles
        }

        return 0
    }

    private suspend fun indexToCleanFiles(
        newFiles: FilesRecords,
        currentVersion: ExtSubmission,
    ): Int {
        val elements = AtomicInteger(0)
        serializationService.filesFlow(currentVersion)
            .withIndex()
            .mapNotNull { (idx, file) ->
                when (newFiles.findMatch(file)) {
                    CONFLICTING -> SubmissionRequestFile(currentVersion, idx + 1, file, RequestFileStatus.CONFLICTING)
                    MatchType.DEPRECATED ->
                        SubmissionRequestFile(
                            currentVersion,
                            idx + 1,
                            file,
                            RequestFileStatus.DEPRECATED,
                        )

                    MatchType.REUSED -> null
                }
            }
            .collect {
                logger.info { "${currentVersion.accNo} ${currentVersion.owner} Indexing to clean file ${it.index}, path='${it.path}'" }
                filesRequestService.saveSubmissionRequestFile(it)
                elements.incrementAndGet()
            }
        return elements.get()
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
            newFile.md5 != existing.md5 && storageMode == existing.storageMode -> CONFLICTING
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
