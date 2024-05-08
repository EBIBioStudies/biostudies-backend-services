package ac.uk.ebi.biostd.persistence.filesystem.api

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.StorageMode
import kotlinx.coroutines.flow.Flow

interface FileStorageService {
    suspend fun releaseSubmissionFile(
        file: ExtFile,
        subRelPath: String,
        subSecretKey: String,
        mode: StorageMode,
    ): ExtFile

    suspend fun suppressSubmissionFile(
        file: ExtFile,
        subRelPath: String,
        subSecretKey: String,
        mode: StorageMode,
    ): ExtFile

    suspend fun persistSubmissionFile(
        sub: ExtSubmission,
        file: ExtFile,
    ): ExtFile

    suspend fun deleteSubmissionFile(
        sub: ExtSubmission,
        file: ExtFile,
    )

    suspend fun deleteSubmissionFiles(
        sub: ExtSubmission,
        process: (Flow<ExtFile>) -> Flow<ExtFile> = { it },
    )

    suspend fun deleteEmptyFolders(sub: ExtSubmission)
}
