package ac.uk.ebi.biostd.persistence.filesystem.service

import ac.uk.ebi.biostd.persistence.filesystem.api.FilePersistenceConfig
import ac.uk.ebi.biostd.persistence.filesystem.api.FileStorageService
import ac.uk.ebi.biostd.persistence.filesystem.fire.FireFilesService
import ac.uk.ebi.biostd.persistence.filesystem.fire.FireFtpService
import ac.uk.ebi.biostd.persistence.filesystem.nfs.NfsFilesService
import ac.uk.ebi.biostd.persistence.filesystem.nfs.NfsFtpService
import ac.uk.ebi.biostd.persistence.filesystem.pagetab.PageTabService
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.StorageMode
import ebi.ac.uk.extended.model.StorageMode.FIRE
import ebi.ac.uk.extended.model.StorageMode.NFS

@Suppress("LongParameterList")
class StorageService(
    private val fireFtpService: FireFtpService,
    private val fireFilesService: FireFilesService,
    private val pageTabService: PageTabService,
    private val nfsFtpService: NfsFtpService,
    private val nfsFilesService: NfsFilesService,
) : FileStorageService {
    override fun preProcessSubmissionFiles(sub: ExtSubmission): FilePersistenceConfig =
        when (sub.storageMode) {
            FIRE -> fireFilesService.preProcessSubmissionFiles(sub)
            NFS -> nfsFilesService.preProcessSubmissionFiles(sub)
        }

    override fun persistSubmissionFile(file: ExtFile, config: FilePersistenceConfig): ExtFile =
        when (config.storageMode) {
            FIRE -> fireFilesService.persistSubmissionFile(file, config)
            NFS -> nfsFilesService.persistSubmissionFile(file, config)
        }

    override fun postProcessSubmissionFiles(config: FilePersistenceConfig) =
        when (config.storageMode) {
            FIRE -> fireFilesService.postProcessSubmissionFiles(config)
            NFS -> nfsFilesService.postProcessSubmissionFiles(config)
        }

    override fun cleanSubmissionFiles(sub: ExtSubmission) =
        when (sub.storageMode) {
            FIRE -> fireFilesService.cleanSubmissionFiles(sub)
            NFS -> nfsFilesService.cleanSubmissionFiles(sub)
        }

    override fun generatePageTab(sub: ExtSubmission): ExtSubmission = pageTabService.generatePageTab(sub)

    override fun releaseSubmissionFile(file: ExtFile, subRelPath: String, mode: StorageMode) =
        when (mode) {
            FIRE -> fireFtpService.releaseSubmissionFile(file, subRelPath)
            NFS -> nfsFtpService.releaseSubmissionFile(file, subRelPath)
        }
}
