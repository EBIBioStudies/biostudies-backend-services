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
import mu.KotlinLogging
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.fileSequence

private val logger = KotlinLogging.logger {}

@Suppress("LongParameterList")
class StorageService(
    private val fireFtpService: FireFtpService,
    private val fireFilesService: FireFilesService,
    private val nfsFtpService: NfsFtpService,
    private val nfsFilesService: NfsFilesService,
    private val serializationService: ExtSerializationService,
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

    override fun deleteSubmissionFile(sub: ExtSubmission, file: ExtFile) = when (sub.storageMode) {
        FIRE -> {
            fireFilesService.deleteSubmissionFile(sub, file)
            fireFilesService.deleteFtpFile(sub, file)
        }

        NFS -> {
            nfsFilesService.deleteSubmissionFile(sub, file)
            nfsFilesService.deleteFtpFile(sub, file)
        }
    }

    override fun deleteSubmissionFiles(
        sub: ExtSubmission,
        process: (Sequence<ExtFile>) -> Sequence<ExtFile>,
    ) {
        process(serializationService.fileSequence(sub)).forEach { file -> deleteSubmissionFile(sub, file) }
        deleteEmptyFolders(sub)
    }

    private fun deleteEmptyFolders(sub: ExtSubmission) = when (sub.storageMode) {
        FIRE -> fireFilesService.deleteEmptyFolders(sub)
        NFS -> nfsFilesService.deleteEmptyFolders(sub)
    }
}
