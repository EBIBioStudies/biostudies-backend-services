package ac.uk.ebi.biostd.persistence.filesystem.api

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.ExtSubmissionInfo

internal interface FilesService {
    suspend fun persistSubmissionFile(
        sub: ExtSubmission,
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

    suspend fun deleteEmptyFolders(sub: ExtSubmission)
}

data class SubStorageInfo(val accNo: String, val owner: String, val secretKey: String, val relPath: String)
