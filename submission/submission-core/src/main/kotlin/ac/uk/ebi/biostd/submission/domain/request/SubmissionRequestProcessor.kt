package ac.uk.ebi.biostd.submission.domain.request

import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus.COPIED
import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus.LOADED
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestFile
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestFilesPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.api.FileStorageService
import ebi.ac.uk.coroutines.chunked
import ebi.ac.uk.coroutines.concurrently
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.model.RequestStatus.CLEANED
import ebi.ac.uk.model.RequestStatus.FILES_COPIED
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.supervisorScope
import mu.KotlinLogging
import java.util.concurrent.atomic.AtomicInteger

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
    suspend fun processRequest(
        accNo: String,
        version: Int,
        processId: String,
    ): SubmissionRequest =
        requestService.onRequest(accNo, version, CLEANED, processId) {
            processRequest(it.process!!.submission)
            it.withNewStatus(FILES_COPIED)
        }

    private suspend fun processRequest(sub: ExtSubmission) {
        logger.info { "${sub.accNo} ${sub.owner} Started persisting submission files on ${sub.storageMode}" }
        persistSubmissionFiles(sub, sub.accNo)
        logger.info { "${sub.accNo} ${sub.owner} Finished persisting submission files on ${sub.storageMode}" }
    }

    private suspend fun persistSubmissionFiles(
        sub: ExtSubmission,
        accNo: String,
    ) {
        val loadedFiles = AtomicInteger()

        suspend fun persistFile(rqtFile: SubmissionRequestFile): SubmissionRequestFile {
            val index = loadedFiles.incrementAndGet()
            logger.info { "$accNo ${sub.owner} Started persisting file $index, path='${rqtFile.path}'" }
            val file =
                when (val persisted = storageService.persistSubmissionFile(sub, rqtFile.file)) {
                    rqtFile.file -> rqtFile.copy(status = COPIED)
                    else -> rqtFile.copy(file = persisted, status = COPIED)
                }
            logger.info { "$accNo ${sub.owner} Finished persisting file $index, path='${rqtFile.path}'" }
            return file
        }

        supervisorScope {
            filesRequestService
                .getSubmissionRequestFiles(accNo, sub.version, LOADED)
                .concurrently(concurrency) { persistFile(it) }
                .chunked(concurrency)
                .onEach { requestService.updateRqtFiles(it) }
                .collect()
        }
    }
}
