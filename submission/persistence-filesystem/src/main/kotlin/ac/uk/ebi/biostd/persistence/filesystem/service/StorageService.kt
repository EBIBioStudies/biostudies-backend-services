package ac.uk.ebi.biostd.persistence.filesystem.service

import ac.uk.ebi.biostd.persistence.filesystem.api.FileStorageService
import ac.uk.ebi.biostd.persistence.filesystem.fire.FireFilesService
import ac.uk.ebi.biostd.persistence.filesystem.fire.FireFtpService
import ac.uk.ebi.biostd.persistence.filesystem.nfs.NfsFilesService
import ac.uk.ebi.biostd.persistence.filesystem.nfs.NfsFtpService
import kotlinx.coroutines.flow.Flow
import uk.ac.ebi.biostd.extended.model.ExtFile
import uk.ac.ebi.biostd.extended.model.ExtSubmission
import uk.ac.ebi.biostd.extended.model.ExtSubmissionInfo
import uk.ac.ebi.biostd.extended.model.FireFile
import uk.ac.ebi.biostd.extended.model.NfsFile
import uk.ac.ebi.biostd.extended.model.RequestFile
import uk.ac.ebi.biostd.extended.model.StorageMode.FIRE
import uk.ac.ebi.biostd.extended.model.StorageMode.NFS
import uk.ac.ebi.biostd.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.biostd.extended.serialization.service.filesFlow
import java.nio.file.Path

@Suppress("LongParameterList")
class StorageService(
    private val fireFtpService: FireFtpService,
    private val fireFilesService: FireFilesService,
    private val nfsFtpService: NfsFtpService,
    private val nfsFilesService: NfsFilesService,
    private val serializationService: ExtSerializationService,
) : FileStorageService {
    override suspend fun persistSubmissionFile(
        sub: ExtSubmissionInfo,
        file: ExtFile,
    ): ExtFile =
        when (sub.storageMode) {
            FIRE -> fireFilesService.persistSubmissionFile(sub, file)
            NFS -> nfsFilesService.persistSubmissionFile(sub, file)
        }

    override suspend fun releaseSubmissionFile(
        sub: ExtSubmissionInfo,
        file: ExtFile,
    ): ExtFile =
        when (sub.storageMode) {
            FIRE -> fireFtpService.releaseSubmissionFile(sub, file)
            NFS -> nfsFtpService.releaseSubmissionFile(sub, file)
        }

    override suspend fun unReleaseSubmissionFile(
        sub: ExtSubmissionInfo,
        file: ExtFile,
    ): ExtFile =
        when (sub.storageMode) {
            FIRE -> fireFtpService.unReleaseSubmissionFile(sub, file)
            NFS -> nfsFtpService.unReleaseSubmissionFile(sub, file)
        }

    override suspend fun deleteSubmissionFile(
        sub: ExtSubmissionInfo,
        file: ExtFile,
    ) = when (sub.storageMode) {
        FIRE -> {
            fireFilesService.deleteSubmissionFile(sub, file)
            fireFilesService.deleteFtpFile(sub, file)
        }

        NFS -> {
            nfsFilesService.deleteSubmissionFile(sub, file)
            nfsFilesService.deleteFtpFile(sub, file)
        }
    }

    override suspend fun deleteSubmissionFiles(
        sub: ExtSubmission,
        process: (Flow<ExtFile>) -> Flow<ExtFile>,
    ) {
        process(serializationService.filesFlow(sub)).collect { file -> deleteSubmissionFile(sub, file) }
        deleteEmptyFolders(sub)
    }

    override suspend fun deleteEmptyFolders(sub: ExtSubmission) =
        when (sub.storageMode) {
            FIRE -> fireFilesService.deleteEmptyFolders(sub)
            NFS -> nfsFilesService.deleteEmptyFolders(sub)
        }

    override suspend fun copyFile(
        file: ExtFile,
        path: Path,
    ) {
        when (file) {
            is FireFile -> fireFilesService.copyFile(file, path.resolve(file.relPath))
            is NfsFile -> nfsFilesService.copyFile(file, path.resolve(file.relPath))
            is RequestFile -> error("Can not copy request file ${file.filePath}")
        }
    }
}
