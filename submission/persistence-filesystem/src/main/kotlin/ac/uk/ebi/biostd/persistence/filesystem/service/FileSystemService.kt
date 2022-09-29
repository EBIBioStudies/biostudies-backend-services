package ac.uk.ebi.biostd.persistence.filesystem.service

import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.StorageMode.FIRE
import ebi.ac.uk.extended.model.StorageMode.NFS

//private val logger = KotlinLogging.logger {}

class FileSystemService(
    private val nfsStorageService: NfsStorageService,
    private val fireStorageService: FireStorageService,
) {
    fun cleanSubmissionFiles(previousSubmission: ExtSubmission) {
        when (previousSubmission.storageMode) {
            NFS -> nfsStorageService.cleanSubmissionFiles(previousSubmission)
            FIRE -> fireStorageService.cleanSubmissionFiles(previousSubmission)
        }
    }

    fun persistSubmissionFiles(sub: ExtSubmission): ExtSubmission {
//        logger.info { "${sub.accNo} ${sub.owner} Started Processing files of submission ${sub.accNo}" }

        return when (sub.storageMode) {
            NFS -> nfsStorageService.persistSubmissionFiles(sub)
            FIRE -> fireStorageService.persistSubmissionFiles(sub)
        }

//        logger.info { "${sub.accNo} ${sub.owner} Finished processing files of submission ${sub.accNo}" }

//        return processedSubmission
    }

    fun releaseSubmissionFiles(sub: ExtSubmission) {
        when(sub.storageMode) {
            NFS -> nfsStorageService.releaseSubmissionFiles(sub)
            FIRE -> fireStorageService.releaseSubmissionFiles(sub)
        }
    }
}
