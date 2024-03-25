package ac.uk.ebi.biostd.submission.domain.request

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.PERSISTED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.PROCESSED
import ac.uk.ebi.biostd.persistence.common.service.RqtResponse
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.api.FileStorageService
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.StorageMode.NFS
import ebi.ac.uk.extended.model.storageMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toSet
import kotlinx.coroutines.flow.withIndex
import mu.KotlinLogging
import uk.ac.ebi.events.service.EventsPublisherService
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.filesFlow

private val logger = KotlinLogging.logger {}

class SubmissionRequestFinalizer(
    private val storageService: FileStorageService,
    private val serializationService: ExtSerializationService,
    private val eventsPublisherService: EventsPublisherService,
    private val queryService: SubmissionPersistenceQueryService,
    private val requestService: SubmissionRequestPersistenceService,
) {
    suspend fun finalizeRequest(accNo: String, version: Int, processId: String): ExtSubmission {
        val (_, submission) = requestService.onRequest(accNo, version, PERSISTED, processId, {
            val sub = finalizeRequest(queryService.getExtByAccNo(accNo, includeFileListFiles = true))
            RqtResponse(it.withNewStatus(PROCESSED), sub)
        })
        eventsPublisherService.submissionFinalized(accNo, version)
        return submission
    }

    private suspend fun finalizeRequest(sub: ExtSubmission): ExtSubmission {
        val previous = queryService.findLatestInactiveByAccNo(sub.accNo, includeFileListFiles = true)

        if (previous != null) deleteRemainingFiles(sub, previous)
        if (sub.storageMode == NFS) storageService.deleteEmptyFolders(sub)

        return sub
    }

    private suspend fun deleteRemainingFiles(current: ExtSubmission, previous: ExtSubmission) {
        val subFiles = subFilesSet(current)
        val accNo = previous.accNo
        val owner = previous.owner

        fun deleteRemainingFiles(allFiles: Flow<ExtFile>): Flow<ExtFile> {
            return allFiles
                .filter { subFiles.contains(it.filePath).not() || it.storageMode != current.storageMode }
                .withIndex()
                .onEach { (i, file) -> logger.info { "$accNo $owner Deleting file $i, path='${file.filePath}'" } }
                .map { it.value }
        }

        logger.info { "$accNo ${previous.owner} Started deleting remaining submission files" }
        storageService.deleteSubmissionFiles(previous, ::deleteRemainingFiles)
        logger.info { "$accNo ${previous.owner} Finished deleting remaining submission files" }
    }

    private suspend fun subFilesSet(sub: ExtSubmission?): Set<String> {
        return when (sub) {
            null -> emptySet()
            else -> serializationService.filesFlow(sub).map { it.filePath }.toSet()
        }
    }
}
