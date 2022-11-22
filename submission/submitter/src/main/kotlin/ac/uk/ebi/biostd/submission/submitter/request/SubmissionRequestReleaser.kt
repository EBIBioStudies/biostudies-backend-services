package ac.uk.ebi.biostd.submission.submitter.request

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.PROCESSED
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestFilesPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.api.FileStorageService
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireFile
import mu.KotlinLogging
import uk.ac.ebi.events.service.EventsPublisherService
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.fileSequence

private val logger = KotlinLogging.logger {}

@Suppress("LongParameterList")
class SubmissionRequestReleaser(
    private val fileStorageService: FileStorageService,
    private val serializationService: ExtSerializationService,
    private val eventsPublisherService: EventsPublisherService,
    private val queryService: SubmissionPersistenceQueryService,
    private val persistenceService: SubmissionPersistenceService,
    private val requestService: SubmissionRequestPersistenceService,
    private val filesRequestService: SubmissionRequestFilesPersistenceService,
) {

    /**
     * Check the release status of the submission and release it if released flag is true.
     */
    fun checkReleased(accNo: String, version: Int): ExtSubmission {
        val request = requestService.getFilesCopiedRequest(accNo, version)
        if (request.submission.released) releaseRequest(request)
        requestService.updateRequestStatus(request.submission.accNo, request.submission.version, PROCESSED)
        eventsPublisherService.submissionSubmitted(accNo, request.notifyTo)
        return request.submission
    }

    private fun releaseRequest(
        request: SubmissionRequest,
    ) {
        val sub = request.submission
        filesRequestService
            .getSubmissionRequestFiles(sub.accNo, sub.version, request.currentIndex)
            .map {
                if (it.file is FireFile && (it.file as FireFile).published) it
                else it.copy(file = fileStorageService.releaseSubmissionFile(it.file, sub.relPath, sub.storageMode))
            }
            .forEach { requestService.updateRequestFile(it) }
        persistenceService.setAsReleased(sub.accNo)
    }

    /**
     * Release the given submission by changing record status database and publishing files.
     */
    fun releaseSubmission(accNo: String) {
        val submission = queryService.getExtByAccNo(accNo, includeFileListFiles = true)
        releaseSubmission(submission)
    }

    /**
     * Generates/refresh FTP status for a given submission.
     */
    fun generateFtp(accNo: String) {
        val sub = queryService.getExtByAccNo(accNo, includeFileListFiles = true)
        releaseSubmission(sub)
    }

    private fun releaseSubmission(sub: ExtSubmission) {
        fun releaseFile(idx: Int, file: ExtFile) {
            logger.info { "${sub.accNo}, ${sub.owner} Started publishing file $idx - ${file.filePath}" }
            fileStorageService.releaseSubmissionFile(file, sub.relPath, sub.storageMode)
            logger.info { "${sub.accNo}, ${sub.owner} Finished publishing file $idx - ${file.filePath}" }
        }

        logger.info { "${sub.accNo} ${sub.owner} Started releasing submission files over ${sub.storageMode}" }
        serializationService.fileSequence(sub)
            .filterNot { it is FireFile && it.published }
            .forEachIndexed { idx, file -> releaseFile(idx, file) }
        persistenceService.setAsReleased(sub.accNo)
        logger.info { "${sub.accNo} ${sub.owner} Finished releasing submission files over ${sub.storageMode}" }
    }
}
