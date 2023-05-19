package ac.uk.ebi.biostd.submission.submitter.request

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.PROCESSED
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.api.FileStorageService
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.storageMode
import mu.KotlinLogging
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.fileSequence

private val logger = KotlinLogging.logger {}

class SubmissionRequestFinalizer(
    private val storageService: FileStorageService,
    private val serializationService: ExtSerializationService,
    private val queryService: SubmissionPersistenceQueryService,
    private val requestService: SubmissionRequestPersistenceService,
) {
    fun finalizeRequest(accNo: String, version: Int): ExtSubmission {
        val request = requestService.getPersistedRequest(accNo, version)
        val sub = queryService.getExtByAccNo(accNo, includeFileListFiles = true)
        val previous = queryService.findLatestInactiveByAccNo(accNo, includeFileListFiles = true)

        if (previous != null) deleteRemainingFiles(sub, previous)

        requestService.saveSubmissionRequest(request.withNewStatus(PROCESSED))
        return sub
    }

    private fun deleteRemainingFiles(current: ExtSubmission?, previous: ExtSubmission) {
        val subFiles = subFilesSet(current)
        val accNo = previous.accNo
        val owner = previous.owner

        fun deleteRemainingFiles(allFiles: Sequence<ExtFile>): Sequence<ExtFile> {
            return allFiles
                .filter { subFiles.contains(it.filePath).not() || it.storageMode != current?.storageMode }
                .onEachIndexed { i, file -> logger.info { "$accNo $owner Deleting file $i, path='${file.filePath}'" } }
        }

        logger.info { "$accNo ${previous.owner} Started deleting remaining submission files" }
        storageService.deleteSubmissionFiles(previous, ::deleteRemainingFiles)
        logger.info { "$accNo ${previous.owner} Finished deleting remaining submission files" }
    }

    private fun subFilesSet(sub: ExtSubmission?): Set<String> {
        return when (sub) {
            null -> emptySet()
            else -> serializationService.fileSequence(sub).mapTo(mutableSetOf()) { it.filePath }
        }
    }
}
