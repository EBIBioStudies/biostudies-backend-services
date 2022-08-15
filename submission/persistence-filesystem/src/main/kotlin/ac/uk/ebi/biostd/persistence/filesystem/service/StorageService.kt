package ac.uk.ebi.biostd.persistence.filesystem.service

import ac.uk.ebi.biostd.persistence.filesystem.api.FileStorageService
import ac.uk.ebi.biostd.persistence.filesystem.fire.FireFilesService
import ac.uk.ebi.biostd.persistence.filesystem.fire.FireFtpService
import ac.uk.ebi.biostd.persistence.filesystem.nfs.NfsFilesService
import ac.uk.ebi.biostd.persistence.filesystem.nfs.NfsFtpService
import ac.uk.ebi.biostd.persistence.filesystem.pagetab.FirePageTabService
import ac.uk.ebi.biostd.persistence.filesystem.pagetab.NfsPageTabService
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.StorageMode

@Suppress("LongParameterList")
class StorageService(
    private val fireFtpService: FireFtpService,
    private val fireFilesService: FireFilesService,
    private val firePageTabService: FirePageTabService,
    private val nfsFtpService: NfsFtpService,
    private val nfsFilesService: NfsFilesService,
    private val nfsPageTabService: NfsPageTabService,
) : FileStorageService {

    override fun persistSubmissionFiles(sub: ExtSubmission): ExtSubmission =
        when (sub.storageMode) {
            StorageMode.FIRE -> fireFilesService.persistSubmissionFiles(sub)
            StorageMode.NFS -> nfsFilesService.persistSubmissionFiles(sub)
        }

    override fun cleanSubmissionFiles(sub: ExtSubmission) =
        when (sub.storageMode) {
            StorageMode.FIRE -> fireFilesService.cleanSubmissionFiles(sub)
            StorageMode.NFS -> nfsFilesService.cleanSubmissionFiles(sub)
        }

    override fun generatePageTab(sub: ExtSubmission): ExtSubmission {
        return when (sub.storageMode) {
            StorageMode.FIRE -> firePageTabService.generatePageTab(sub)
            StorageMode.NFS -> nfsPageTabService.generatePageTab(sub)
        }
    }

    override fun releaseSubmissionFiles(sub: ExtSubmission) =
        when (sub.storageMode) {
            StorageMode.FIRE -> fireFtpService.releaseSubmissionFiles(sub)
            StorageMode.NFS -> nfsFtpService.releaseSubmissionFiles(sub)
        }

    override fun generateFtpLinks(sub: ExtSubmission) =
        when (sub.storageMode) {
            StorageMode.FIRE -> fireFtpService.generateFtpLinks(sub)
            StorageMode.NFS -> nfsFtpService.generateFtpLinks(sub)
        }
}
