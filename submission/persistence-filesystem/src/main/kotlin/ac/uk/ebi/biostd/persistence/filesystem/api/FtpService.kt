package ac.uk.ebi.biostd.persistence.filesystem.api

import ebi.ac.uk.extended.model.ExtFile

internal interface FtpService {
    fun releaseSubmissionFile(file: ExtFile, subRelPath: String): ExtFile
}
