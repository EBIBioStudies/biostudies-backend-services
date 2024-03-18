package ac.uk.ebi.biostd.persistence.filesystem.api

import ebi.ac.uk.extended.model.ExtFile

internal interface FtpService {
    suspend fun releaseSubmissionFile(file: ExtFile, subRelPath: String, subSecretKey: String): ExtFile
}
