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
    override fun persistSubmissionFile(sub: ExtSubmission, file: ExtFile): ExtFile =
        when (sub.storageMode) {
            FIRE -> fireFilesService.persistSubmissionFile(sub, file)
            NFS -> nfsFilesService.persistSubmissionFile(sub, file)
        }

    override fun releaseSubmissionFile(file: ExtFile, subRelPath: String, mode: StorageMode): ExtFile {
        return when (mode) {
            FIRE -> fireFtpService.releaseSubmissionFile(file, subRelPath)
            NFS -> nfsFtpService.releaseSubmissionFile(file, subRelPath)
        }
    }

    override fun deleteFtpFile(sub: ExtSubmission, file: ExtFile) =
        when (sub.storageMode) {
            FIRE -> fireFilesService.deleteFtpFile(sub, file)
            NFS -> nfsFilesService.deleteFtpFile(sub, file)
        }

    override fun deleteSubmissionFile(sub: ExtSubmission, file: ExtFile) =
        when (sub.storageMode) {
            FIRE -> fireFilesService.deleteSubmissionFile(sub, file)
            NFS -> nfsFilesService.deleteSubmissionFile(sub, file)
        }

    override fun deleteSubmissionFiles(sub: ExtSubmission) =
        when (sub.storageMode) {
            FIRE -> fireFilesService.deleteSubmissionFiles(sub)
            NFS -> nfsFilesService.deleteSubmissionFiles(sub)
        }
}
