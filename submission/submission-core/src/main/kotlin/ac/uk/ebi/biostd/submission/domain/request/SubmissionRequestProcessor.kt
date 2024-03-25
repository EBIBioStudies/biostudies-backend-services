package ac.uk.ebi.biostd.submission.domain.request

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.CLEANED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.FILES_COPIED
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestFile
import ac.uk.ebi.biostd.persistence.common.service.RqtUpdate
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestFilesPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.api.FileStorageService
import ebi.ac.uk.extended.model.ExtSubmission
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.supervisorScope
import mu.KotlinLogging
import uk.ac.ebi.events.service.EventsPublisherService

private val logger = KotlinLogging.logger {}

class SubmissionRequestProcessor(
    private val concurrency: Int,
    private val storageService: FileStorageService,
    private val eventsPublisherService: EventsPublisherService,
    private val requestService: SubmissionRequestPersistenceService,
    private val filesRequestService: SubmissionRequestFilesPersistenceService,
) {
    /**
     * Process the current submission files. Note that [ExtSubmission] returned does not include file list files.
     */
    suspend fun processRequest(accNo: String, version: Int, processId: String) {
        requestService.onRequest(accNo, version, CLEANED, processId, {
            processRequest(it.submission, it.currentIndex)
            RqtUpdate(it.withNewStatus(FILES_COPIED))
        })
        eventsPublisherService.requestFilesCopied(accNo, version)
    }

    private suspend fun processRequest(sub: ExtSubmission, currentIndex: Int) {
        logger.info { "${sub.accNo} ${sub.owner} Started persisting submission files on ${sub.storageMode}" }
        persistSubmissionFiles(sub, sub.accNo, sub.version, currentIndex)
        logger.info { "${sub.accNo} ${sub.owner} Finished persisting submission files on ${sub.storageMode}" }
    }

    private suspend fun persistSubmissionFiles(sub: ExtSubmission, accNo: String, version: Int, startingAt: Int) {
        suspend fun persistFile(rqtFile: SubmissionRequestFile) {
            logger.info { "$accNo ${sub.owner} Started persisting file ${rqtFile.index}, path='${rqtFile.path}'" }
            when (val persisted = storageService.persistSubmissionFile(sub, rqtFile.file)) {
                rqtFile.file -> requestService.updateRqtIndex(accNo, version, rqtFile.index)
                else -> requestService.updateRqtIndex(rqtFile, persisted)
            }
            logger.info { "$accNo ${sub.owner} Finished persisting file ${rqtFile.index}, path='${rqtFile.path}'" }
        }

        supervisorScope {
            filesRequestService
                .getSubmissionRequestFiles(accNo, sub.version, startingAt)
                .map { async { persistFile(it) } }
                .buffer(concurrency)
                .collect { it.await() }
        }
    }
}
