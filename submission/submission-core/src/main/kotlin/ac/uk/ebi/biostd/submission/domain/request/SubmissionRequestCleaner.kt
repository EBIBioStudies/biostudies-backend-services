package ac.uk.ebi.biostd.submission.domain.request

import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus
import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus.CONFLICTING
import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus.DEPRECATED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.CLEANED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.INDEXED_CLEANED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.PERSISTED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.PROCESSED
import ac.uk.ebi.biostd.persistence.common.service.RqtUpdate
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestFilesPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.api.FileStorageService
import ebi.ac.uk.extended.model.ExtFile
import kotlinx.coroutines.flow.withIndex
import mu.KotlinLogging
import uk.ac.ebi.events.service.EventsPublisherService

private val logger = KotlinLogging.logger {}

class SubmissionRequestCleaner(
    private val concurrency: Int,
    private val queryService: SubmissionPersistenceQueryService,
    private val storageService: FileStorageService,
    private val eventsPublisherService: EventsPublisherService,
    private val requestService: SubmissionRequestPersistenceService,
    private val filesRequestService: SubmissionRequestFilesPersistenceService,
) {
    suspend fun cleanCurrentVersion(
        accNo: String,
        version: Int,
        processId: String,
    ) {
        requestService.onRequest(accNo, version, INDEXED_CLEANED, processId, {
            val previousVersion = it.previousVersion
            if (previousVersion != null) {
                cleanFiles(
                    accNo = accNo,
                    version = version,
                    previousVersion = previousVersion,
                    status = CONFLICTING,
                )
            }
            RqtUpdate(it.withNewStatus(CLEANED))
        })
        eventsPublisherService.requestCleaned(accNo, version)
    }

    suspend fun finalizeRequest(
        accNo: String,
        version: Int,
        processId: String,
    ) {
        requestService.onRequest(accNo, version, PERSISTED, processId, {
            val previousVersion = it.previousVersion
            if (previousVersion != null) {
                cleanFiles(
                    accNo = accNo,
                    version = version,
                    previousVersion = -previousVersion,
                    status = DEPRECATED,
                )
            }
            RqtUpdate(it.withNewStatus(PROCESSED))
        })
        eventsPublisherService.submissionFinalized(accNo, version)
    }

    private suspend fun cleanFiles(
        accNo: String,
        version: Int,
        previousVersion: Int,
        status: RequestFileStatus,
    ) {
        val sub = queryService.getBasicByAccNoAndVersion(accNo, previousVersion)

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
