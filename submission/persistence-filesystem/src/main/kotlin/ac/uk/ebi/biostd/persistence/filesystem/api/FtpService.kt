package ac.uk.ebi.biostd.persistence.filesystem.api

interface FtpService {
    fun releaseSubmissionFiles(accNo: String, owner: String, relPath: String)
    fun generateFtpLinks(accNo: String)
}
