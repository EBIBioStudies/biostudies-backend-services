package ac.uk.ebi.biostd.persistence.filesystem.api

import uk.ac.ebi.biostd.extended.model.ExtFile
import uk.ac.ebi.biostd.extended.model.ExtSubmissionInfo

internal interface FtpService {
    suspend fun releaseSubmissionFile(
        sub: ExtSubmissionInfo,
        file: ExtFile,
    ): ExtFile

    suspend fun unReleaseSubmissionFile(
        sub: ExtSubmissionInfo,
        file: ExtFile,
    ): ExtFile
}
