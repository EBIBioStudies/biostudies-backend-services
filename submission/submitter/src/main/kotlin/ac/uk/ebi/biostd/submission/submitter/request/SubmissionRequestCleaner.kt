package ac.uk.ebi.biostd.submission.submitter.request

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.CLEANED
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestFilesPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.api.FileStorageService
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.StorageMode
import ebi.ac.uk.extended.model.storageMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.fileSequence

private val logger = KotlinLogging.logger {}

class SubmissionRequestCleaner(
    private val concurrency: Int,
    private val storageService: FileStorageService,
    private val serializationService: ExtSerializationService,
    private val queryService: SubmissionPersistenceQueryService,
    private val requestService: SubmissionRequestPersistenceService,
    private val filesRequestService: SubmissionRequestFilesPersistenceService,
) {
    fun cleanCurrentVersion(accNo: String, version: Int) {
        val request = requestService.getLoadedRequest(accNo, version)
        val new = request.submission
        val current = queryService.findExtByAccNo(accNo, includeFileListFiles = true)

        if (current != null) {
            logger.info { "${new.accNo} ${new.owner} Started cleaning common files of version ${new.version}" }
            deleteCommonFiles(new, current)
            logger.info { "${new.accNo} ${new.owner} Finished cleaning common files of version ${new.version}" }
        }

        requestService.saveSubmissionRequest(request.withNewStatus(status = CLEANED))
    }

    private fun deleteCommonFiles(new: ExtSubmission, current: ExtSubmission) {
        fun deleteFile(index: Int, file: ExtFile) {
            logger.info { "${current.accNo} ${current.owner} Deleting file $index, path='${file.filePath}'" }
            storageService.deleteSubmissionFile(current, file)
        }

        fun shouldDelete(newFiles: Map<String, FileEntry>, existing: ExtFile): Boolean =
            when (val newFile = newFiles[existing.filePath]) {
                null -> false
                else -> newFile.md5 != existing.md5 && new.storageMode == existing.storageMode
            }

        val newFiles = runBlocking { newFilesMap(new) }
        logger.info { "${current.accNo} ${current.owner} Started cleaning common submission files" }
        serializationService.fileSequence(current)
            .filter { shouldDelete(newFiles, it) }
            .forEachIndexed { index, file -> deleteFile(index, file) }
        logger.info { "${current.accNo} ${current.owner} Finished cleaning common submission files" }
    }

    private suspend fun newFilesMap(new: ExtSubmission): Map<String, FileEntry> {
        var files: Map<String, FileEntry>
        withContext(Dispatchers.Default) {
            files = filesRequestService
                .getSubmissionRequestFiles(new.accNo, new.version, 0)
                .map { it.file }
                .buffer(concurrency)
                .toList()
                .associate { it.filePath to FileEntry(it.md5, new.storageMode) }
        }

        return files
    }

    private data class FileEntry(val md5: String, val storageMode: StorageMode)
}
