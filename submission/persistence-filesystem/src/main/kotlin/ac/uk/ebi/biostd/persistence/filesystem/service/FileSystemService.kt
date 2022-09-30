package ac.uk.ebi.biostd.persistence.filesystem.service

import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.StorageMode.FIRE
import ebi.ac.uk.extended.model.StorageMode.NFS

// TODO not needed
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
        return when (sub.storageMode) {
            NFS -> nfsStorageService.persistSubmissionFiles(sub)
            FIRE -> fireStorageService.persistSubmissionFiles(sub)
        }
    }

    fun releaseSubmissionFiles(sub: ExtSubmission) {
        when(sub.storageMode) {
            NFS -> nfsStorageService.releaseSubmissionFiles(sub)
            FIRE -> fireStorageService.releaseSubmissionFiles(sub)
        }
    }
}
