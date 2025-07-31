package ac.uk.ebi.biostd.persistence.filesystem.api

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmissionInfo
import java.nio.file.Path

internal interface FilesService {
    suspend fun persistSubmissionFile(
        sub: ExtSubmissionInfo,
        file: ExtFile,
    ): ExtFile

    suspend fun deleteSubmissionFile(
        sub: ExtSubmissionInfo,
        file: ExtFile,
    )

    suspend fun deleteFtpFile(
        sub: ExtSubmissionInfo,
        file: ExtFile,
    )

    suspend fun deleteEmptyFolders(sub: ExtSubmissionInfo)

    suspend fun copyFile(
        file: ExtFile,
        targetFilePath: Path,
    )
}
