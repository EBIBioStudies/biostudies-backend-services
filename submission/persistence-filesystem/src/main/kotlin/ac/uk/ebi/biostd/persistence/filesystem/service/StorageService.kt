package ac.uk.ebi.biostd.persistence.filesystem.service

import ac.uk.ebi.biostd.persistence.filesystem.api.FileStorageService
import ac.uk.ebi.biostd.persistence.filesystem.fire.FireFilesService
import ac.uk.ebi.biostd.persistence.filesystem.fire.FireFtpService
import ac.uk.ebi.biostd.persistence.filesystem.nfs.NfsFilesService
import ac.uk.ebi.biostd.persistence.filesystem.nfs.NfsFtpService
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.StorageMode
import ebi.ac.uk.extended.model.StorageMode.FIRE
import ebi.ac.uk.extended.model.StorageMode.NFS

@Suppress("LongParameterList")
class StorageService(
    private val fireFtpService: FireFtpService,
    private val fireFilesService: FireFilesService,
    private val nfsFtpService: NfsFtpService,
    private val nfsFilesService: NfsFilesService,
) : FileStorageService {
    override fun prepareSubmissionFiles(new: ExtSubmission, current: ExtSubmission?) {
        if (current != null && new.storageMode == current.storageMode) {
            when(new.storageMode) {
                FIRE -> fireFilesService.cleanCommonFiles(new, current)
                NFS -> nfsFilesService.cleanCommonFiles(new, current)
            }
        }
    }

    override fun persistSubmissionFile(sub: ExtSubmission, file: ExtFile): ExtFile =
        when (sub.storageMode) {
            FIRE -> fireFilesService.persistSubmissionFile(sub, file)
            NFS -> nfsFilesService.persistSubmissionFile(sub, file)
        }

    override fun postProcessSubmissionFiles(new: ExtSubmission, current: ExtSubmission?) {
        postProcessSubmissionFiles(new)

        if (current != null) {
            if (new.storageMode == current.storageMode) cleanRemainingFiles(new, current)
            else cleanSubmissionFiles(current)
        }
    }

    private fun cleanRemainingFiles(new: ExtSubmission, previous: ExtSubmission) =
        when(new.storageMode) {
            FIRE -> fireFilesService.cleanRemainingFiles(new, previous)
            NFS -> nfsFilesService.cleanRemainingFiles(new, previous)
        }

    private fun postProcessSubmissionFiles(sub: ExtSubmission) =
        when(sub.storageMode) {
            FIRE -> fireFilesService.postProcessSubmissionFiles(sub)
            NFS -> nfsFilesService.postProcessSubmissionFiles(sub)
        }

    override fun cleanSubmissionFiles(sub: ExtSubmission) =
        when (sub.storageMode) {
            FIRE -> fireFilesService.cleanSubmissionFiles(sub)
            NFS -> nfsFilesService.cleanSubmissionFiles(sub)
        }

    override fun releaseSubmissionFile(file: ExtFile, subRelPath: String, mode: StorageMode): ExtFile {
        return when (mode) {
            FIRE -> fireFtpService.releaseSubmissionFile(file, subRelPath)
            NFS -> nfsFtpService.releaseSubmissionFile(file, subRelPath)
        }
    }
}
