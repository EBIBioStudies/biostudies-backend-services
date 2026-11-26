package ac.uk.ebi.biostd.persistence.filesystem.api

import kotlinx.coroutines.flow.Flow
import uk.ac.ebi.biostd.extended.model.ExtFile
import uk.ac.ebi.biostd.extended.model.ExtSubmission
import uk.ac.ebi.biostd.extended.model.ExtSubmissionInfo
import java.nio.file.Path

interface FileStorageService {
    suspend fun releaseSubmissionFile(
        sub: ExtSubmissionInfo,
        file: ExtFile,
    ): ExtFile

    suspend fun unReleaseSubmissionFile(
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

    suspend fun copyFile(
        file: ExtFile,
        path: Path,
    )
}
