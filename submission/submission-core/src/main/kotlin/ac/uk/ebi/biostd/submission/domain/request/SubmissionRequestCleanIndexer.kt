package ac.uk.ebi.biostd.submission.domain.request

import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus
import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus.CONFLICTING
import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus.CONFLICTING_PAGE_TAB
import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus.DEPRECATED
import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus.DEPRECATED_PAGE_TAB
import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus.LOADED
import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus.REUSED
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestFileChanges
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestFilesPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.PersistedExtFile
import ebi.ac.uk.extended.model.StorageMode
import ebi.ac.uk.extended.model.allPageTabFiles
import ebi.ac.uk.extended.model.storageMode
import ebi.ac.uk.model.RequestStatus
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import mu.KotlinLogging
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.filesFlowExt
import java.util.concurrent.atomic.AtomicInteger
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestFile as SubRqtFile

private val logger = KotlinLogging.logger {}

class SubmissionRequestCleanIndexer(
    private val serializationService: ExtSerializationService,
    private val queryService: SubmissionPersistenceQueryService,
    private val filesRequestService: SubmissionRequestFilesPersistenceService,
    private val requestService: SubmissionRequestPersistenceService,
) {
    /**
     * Index submission request to clean files by creating records for each one.
     */
    suspend fun indexToCleanRequest(
        accNo: String,
        version: Int,
        processId: String,
    ): SubmissionRequest =
        requestService.onRequest(accNo, version, RequestStatus.LOADED, processId) {
            val indexedRequest = indexRequest(it.process!!.submission)
            it.cleanIndexed(
                previousVersion = indexedRequest.first,
                fileChanges = indexedRequest.second,
            )
        }

    internal suspend fun indexRequest(new: ExtSubmission): Pair<Int?, SubmissionRequestFileChanges> {
        val current = queryService.findExtByAccNo(new.accNo, includeFileListFiles = true)

        if (current != null) {
            logger.info { "${new.accNo} ${new.owner} Started indexing submission files to be cleaned" }
            val newFiles = summarizeFileRecords(new)
            val response = indexToCleanFiles(new = new, newFiles = newFiles, current = current)
            logger.info { "${new.accNo} ${new.owner} Finished indexing submission files to be cleaned" }

            return current.version to response
        }

        return null to SubmissionRequestFileChanges(0, 0, 0, 0, 0)
    }

    private suspend fun indexToCleanFiles(
        new: ExtSubmission,
        newFiles: FilesRecords,
        current: ExtSubmission,
    ): SubmissionRequestFileChanges {
        val reusedIdx = AtomicInteger(0)
        val conflictIdx = AtomicInteger(0)
        val conflictPageTabIdx = AtomicInteger(0)
        val deprecatedIdx = AtomicInteger(0)
        val deprecatedPageTabIdx = AtomicInteger(0)

        fun fileUpdate(
            idx: AtomicInteger,
            file: ExtFile,
            status: RequestFileStatus,
        ) = SubRqtFile(new, idx.incrementAndGet(), file, status, true)

        serializationService
            .filesFlowExt(current)
            .mapNotNull { (isPageTab, file) ->
                require(file is PersistedExtFile) { "Only persisted files are supported" }

                when (newFiles.findMatch(file, isPageTab)) {
                    MatchType.CONFLICTING -> fileUpdate(conflictIdx, file, CONFLICTING)
                    MatchType.CONFLICTING_PAGE_TAB -> fileUpdate(conflictPageTabIdx, file, CONFLICTING_PAGE_TAB)
                    MatchType.DEPRECATED -> fileUpdate(deprecatedIdx, file, DEPRECATED)
                    MatchType.DEPRECATED_PAGE_TAB -> fileUpdate(deprecatedPageTabIdx, file, DEPRECATED_PAGE_TAB)
                    MatchType.REUSED -> fileUpdate(reusedIdx, file, REUSED)
                }
            }.collect {
                logger.info { "${new.accNo} ${new.owner} Indexing to clean file ${it.index}, path='${it.path}'" }
                filesRequestService.saveSubmissionRequestFile(it)
            }

        return SubmissionRequestFileChanges(
            reusedIdx.get(),
            deprecatedIdx.get(),
            deprecatedPageTabIdx.get(),
            conflictIdx.get(),
            conflictPageTabIdx.get(),
        )
    }

    private suspend fun summarizeFileRecords(new: ExtSubmission): FilesRecords {
        val response = mutableMapOf<String, FileRecord>()
        val pageTabFiles = new.allPageTabFiles.filterIsInstance<PersistedExtFile>().groupBy { it.md5 }

        filesRequestService
            .getSubmissionRequestFiles(new.accNo, new.version, LOADED)
            .map { it.file }
            .filterIsInstance<PersistedExtFile>()
            .collect { response[it.filePath] = FileRecord(it.md5, new.storageMode, pageTabFiles.containsKey(it.md5)) }
        return FilesRecords(response)
    }
}

/**
 * Contains new submission file entries and storage type.
 */
private class FilesRecords(
    val newFiles: Map<String, FileRecord>,
) {
    /**
     * Identifies and classifies the given file in one of the five categories:
     * - DEPRECATED: The existing file is not present in the new version or the storage mode has changed
     * - DEPRECATED_PAGE_TAB: The existing pagetab file is not present in the new version and storage mode has changed
     * - CONFLICTING: The existing file is present in the new version but with different content
     * - CONFLICTING_PAGE_TAB: The existing pagetab file is present in the new version but with different content
     * - REUSED: The existing file hasn't changed in the new version, so it can be reused
     */
    fun findMatch(
        existing: PersistedExtFile,
        isPageTab: Boolean,
    ): MatchType {
        val newFile = newFiles[existing.filePath]
        val storageModeChanged = newFile?.storageMode != existing.storageMode
        val md5Changed = newFile?.md5 != existing.md5

        return when {
            newFile != null && storageModeChanged && isPageTab -> MatchType.DEPRECATED_PAGE_TAB
            newFile == null || storageModeChanged -> MatchType.DEPRECATED
            md5Changed && isPageTab -> MatchType.CONFLICTING_PAGE_TAB
            md5Changed -> MatchType.CONFLICTING
            else -> MatchType.REUSED
        }
    }
}

private enum class MatchType {
    CONFLICTING,
    CONFLICTING_PAGE_TAB,
    DEPRECATED,
    DEPRECATED_PAGE_TAB,
    REUSED,
}

private data class FileRecord(
    val md5: String,
    val storageMode: StorageMode,
    val isPageTab: Boolean,
)
