package ac.uk.ebi.biostd.submission.submitter.request

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.FILES_COPIED
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestFile
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestFilesPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.api.FileStorageService
import ebi.ac.uk.extended.model.ExtSubmission
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Mutex
import mu.KotlinLogging
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger {}

class SubmissionRequestProcessor(
    private val concurrency: Int,
    private val storageService: FileStorageService,
    private val requestService: SubmissionRequestPersistenceService,
    private val filesRequestService: SubmissionRequestFilesPersistenceService,
) {
    /**
     * Process the current submission files. Note that [ExtSubmission] returned does not include file list files.
     */
    suspend fun processRequest(accNo: String, version: Int) {
        val request = requestService.getCleanedRequest(accNo, version)
        val sub = request.submission

        logger.info { "$accNo ${sub.owner} Started persisting submission files on ${sub.storageMode}" }
        persistSubmissionFiles(sub, accNo, version, request.currentIndex)
        requestService.saveSubmissionRequest(request.withNewStatus(FILES_COPIED))
        logger.info { "$accNo ${sub.owner} Finished persisting submission files on ${sub.storageMode}" }
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

        val mutexMap = ConcurrentHashMap<String, Mutex>()
        supervisorScope {
            filesRequestService
                .getSubmissionRequestFiles(accNo, sub.version, startingAt)
                .map {
                    val mutex = mutexMap.getOrPut(it.file.md5) { Mutex() }.apply { lock() }
                    mutex to async { persistFile(it) }
                }
                .buffer(concurrency)
                .collect { (mutex, job) -> job.await(); mutex.unlock() }
        }
    }
}
