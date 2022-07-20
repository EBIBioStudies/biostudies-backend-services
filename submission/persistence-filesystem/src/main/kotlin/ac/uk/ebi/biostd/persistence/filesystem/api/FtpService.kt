package ac.uk.ebi.biostd.persistence.filesystem.api

import ebi.ac.uk.extended.model.ExtSubmission

interface FtpService {
    fun releaseSubmissionFiles(sub: ExtSubmission)
    fun generateFtpLinks(accNo: String)
}
