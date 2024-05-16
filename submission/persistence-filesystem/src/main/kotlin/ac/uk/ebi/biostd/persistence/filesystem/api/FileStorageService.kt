package ac.uk.ebi.biostd.persistence.filesystem.api

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.ExtSubmissionInfo
import kotlinx.coroutines.flow.Flow

interface FileStorageService {
    suspend fun releaseSubmissionFile(
        sub: ExtSubmissionInfo,
        file: ExtFile,
    ): ExtFile

    suspend fun persistSubmissionFile(
        sub: ExtSubmissionInfo,
        file: ExtFile,
    ): ExtFile

    suspend fun deleteSubmissionFile(
        sub: ExtSubmissionInfo,
        file: ExtFile,
    )

    suspend fun deleteSubmissionFiles(
        sub: ExtSubmission,
        process: (Flow<ExtFile>) -> Flow<ExtFile> = { it },
    )

    suspend fun deleteEmptyFolders(sub: ExtSubmission)
}
