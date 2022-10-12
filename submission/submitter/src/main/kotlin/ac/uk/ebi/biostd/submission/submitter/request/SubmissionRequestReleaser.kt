package ac.uk.ebi.biostd.submission.submitter.request

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.PROCESSED
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.api.FileStorageService
import ebi.ac.uk.extended.model.ExtSubmission
import mu.KotlinLogging
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.forEachFile

private val logger = KotlinLogging.logger {}

class SubmissionRequestReleaser(
    private val fileStorageService: FileStorageService,
    private val serializationService: ExtSerializationService,
    private val queryService: SubmissionPersistenceQueryService,
    private val persistenceService: SubmissionPersistenceService,
    private val requestService: SubmissionRequestPersistenceService,
) {
    /**
     * Check the release status of the submission and release it if released flag is true.
     */
    fun checkReleased(accNo: String, version: Int): ExtSubmission {
        val sub = queryService.getExtByAccNoAndVersion(accNo, version, includeFileListFiles = true)
        if (sub.released) releaseSubmission(sub)
        requestService.updateRequestStatus(sub.accNo, sub.version, PROCESSED)
        return sub
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
        logger.info { "${sub.accNo} ${sub.owner} Started releasing submission files over ${sub.storageMode}" }

        persistenceService.setAsReleased(sub.accNo)
        serializationService.forEachFile(sub) { file, idx ->
            logger.info { "${sub.accNo}, ${sub.owner} Publishing file $idx - ${file.filePath}" }
            fileStorageService.releaseSubmissionFile(file, sub.relPath, sub.storageMode)
        }

        logger.info { "${sub.accNo} ${sub.owner} Finished releasing submission files over ${sub.storageMode}" }
    }
}
