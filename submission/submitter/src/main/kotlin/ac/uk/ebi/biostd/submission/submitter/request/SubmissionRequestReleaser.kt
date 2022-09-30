package ac.uk.ebi.biostd.submission.submitter.request

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.PROCESSED
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.fire.FireFtpService
import ac.uk.ebi.biostd.persistence.filesystem.nfs.NfsFtpService
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.StorageMode.FIRE
import ebi.ac.uk.extended.model.StorageMode.NFS
import mu.KotlinLogging
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.forEachFile

private val logger = KotlinLogging.logger {}

class SubmissionRequestReleaser(
//    private val fileSystemService: FileSystemService,
    private val nfsFtpService: NfsFtpService,
    private val fireFtpService: FireFtpService,
    private val serializationService: ExtSerializationService,
    private val queryService: SubmissionPersistenceQueryService,
    private val persistenceService: SubmissionPersistenceService,
) {
    /**
     * Check the release status of the submission and release it if released flag is true.
     */
    fun checkReleased(accNo: String, version: Int): ExtSubmission {
        val sub = queryService.getExtByAccNoAndVersion(accNo, version, includeFileListFiles = true)
        if (sub.released) releaseSubmission(sub)
        persistenceService.updateRequestStatus(sub.accNo, sub.version, PROCESSED)
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
        releaseSubmissionFiles(sub)
    }

    private fun releaseSubmission(sub: ExtSubmission) {
        logger.info { "${sub.accNo} ${sub.owner} Releasing submission ${sub.accNo}" }

        persistenceService.setAsReleased(sub.accNo)
        releaseSubmissionFiles(sub)

        logger.info { "${sub.accNo} ${sub.owner} released submission ${sub.accNo}" }
    }

    private fun releaseSubmissionFiles(sub: ExtSubmission) = when (sub.storageMode) {
        NFS -> releaseNfsFiles(sub)
        FIRE -> releaseFireFiles(sub)
    }

    private fun releaseFireFiles(sub: ExtSubmission) {
        logger.info { "${sub.accNo} ${sub.owner} Started processing FTP links over FIRE" }

        serializationService.forEachFile(sub) { file, idx ->
            if (file is FireFile) {
                logger.info { "${sub.accNo} ${sub.owner} Started publishing file $idx, fireId='${file.fireId}'" }

                fireFtpService.releaseSubmissionFile(file)
                persistenceService.updateRequestIndex(sub.accNo, sub.version, idx)

                logger.info { "${sub.accNo} ${sub.owner} Finished publishing file $idx, fireId='${file.fireId}'" }
            }
        }

        logger.info { "${sub.accNo} ${sub.owner} Finished processing FTP links over FIRE" }
    }

    private fun releaseNfsFiles(sub: ExtSubmission) {
        nfsFtpService.releaseSubmissionFiles(sub)
    }
}
