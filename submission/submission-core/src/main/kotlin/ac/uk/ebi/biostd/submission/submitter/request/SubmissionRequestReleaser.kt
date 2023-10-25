package ac.uk.ebi.biostd.submission.submitter.request

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.CHECK_RELEASED
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestFile
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestFilesPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.api.FileStorageService
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.supervisorScope
import mu.KotlinLogging
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.filesFlow

private val logger = KotlinLogging.logger {}

@Suppress("LongParameterList")
class SubmissionRequestReleaser(
    private val concurrency: Int,
    private val fileStorageService: FileStorageService,
    private val serializationService: ExtSerializationService,
    private val queryService: SubmissionPersistenceQueryService,
    private val persistenceService: SubmissionPersistenceService,
    private val requestService: SubmissionRequestPersistenceService,
    private val filesRequestService: SubmissionRequestFilesPersistenceService,
) {
    /**
     * Check the release status of the submission and release it if released flag is true.
     */
    suspend fun checkReleased(accNo: String, version: Int) {
        val request = requestService.getFilesCopiedRequest(accNo, version)
        if (request.submission.released) releaseRequest(accNo, version, request)
        requestService.saveSubmissionRequest(request.withNewStatus(CHECK_RELEASED))
    }

    private suspend fun releaseRequest(
        accNo: String,
        version: Int,
        request: SubmissionRequest,
    ) {
        val sub = request.submission
        logger.info { "$accNo ${sub.owner} Started releasing submission files over ${sub.storageMode}" }
        releaseSubmissionFiles(accNo, version, sub, request.currentIndex)
        logger.info { "$accNo ${sub.owner} Finished releasing submission files over ${sub.storageMode}" }
    }

    private suspend fun releaseSubmissionFiles(accNo: String, version: Int, sub: ExtSubmission, startingAt: Int) {
        suspend fun releaseFile(reqFile: SubmissionRequestFile) {
            when (val file = reqFile.file) {
                is NfsFile ->
                    requestService.updateRqtIndex(reqFile, releaseFile(sub, reqFile.index, file))

                is FireFile -> {
                    if (file.published) requestService.updateRqtIndex(accNo, version, reqFile.index)
                    else requestService.updateRqtIndex(reqFile, releaseFile(sub, reqFile.index, file))
                }
            }
        }

        supervisorScope {
            filesRequestService
                .getSubmissionRequestFiles(sub.accNo, sub.version, startingAt)
                .map { async { releaseFile(it) } }
                .buffer(concurrency)
                .collect { it.await() }
        }
    }

    /**
     * Release the given submission by changing record status database and publishing files.
     */
    suspend fun releaseSubmission(accNo: String) {
        val submission = queryService.getExtByAccNo(accNo, includeFileListFiles = true)
        releaseSubmission(submission)
    }

    /**
     * Generates/refresh FTP status for a given submission.
     */
    suspend fun generateFtp(accNo: String) {
        val sub = queryService.getExtByAccNo(accNo, includeFileListFiles = true)
        releaseSubmission(sub)
    }

    private suspend fun releaseFile(sub: ExtSubmission, idx: Int, file: ExtFile): ExtFile {
        logger.info { "${sub.accNo}, ${sub.owner} Started publishing file $idx - ${file.filePath}" }
        val releasedFile = fileStorageService.releaseSubmissionFile(file, sub.relPath, sub.storageMode)
        logger.info { "${sub.accNo}, ${sub.owner} Finished publishing file $idx - ${file.filePath}" }
        return releasedFile
    }

    private suspend fun releaseSubmission(sub: ExtSubmission) {
        logger.info { "${sub.accNo} ${sub.owner} Started releasing submission files over ${sub.storageMode}" }
        serializationService.filesFlow(sub)
            .filterNot { it is FireFile && it.published }
            .collectIndexed { idx, file -> releaseFile(sub, idx, file) }
        persistenceService.setAsReleased(sub.accNo)
        logger.info { "${sub.accNo} ${sub.owner} Finished releasing submission files over ${sub.storageMode}" }
    }
}
