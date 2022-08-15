package ac.uk.ebi.biostd.persistence.filesystem.api

import ebi.ac.uk.extended.model.ExtSubmission

interface FileStorageService {
    fun releaseSubmissionFiles(sub: ExtSubmission)
    fun generateFtpLinks(sub: ExtSubmission)
    fun persistSubmissionFiles(sub: ExtSubmission): ExtSubmission
    fun cleanSubmissionFiles(sub: ExtSubmission)
    fun generatePageTab(sub: ExtSubmission): ExtSubmission
}
