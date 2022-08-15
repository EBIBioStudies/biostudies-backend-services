package ac.uk.ebi.biostd.persistence.filesystem.api

import ebi.ac.uk.extended.model.ExtSubmission

internal interface FtpService {
    fun releaseSubmissionFiles(sub: ExtSubmission)
    fun generateFtpLinks(sub: ExtSubmission)
}
