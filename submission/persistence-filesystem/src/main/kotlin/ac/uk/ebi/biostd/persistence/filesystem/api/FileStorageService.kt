package ac.uk.ebi.biostd.persistence.filesystem.api

import ebi.ac.uk.extended.model.ExtSubmission

// TODO not needed
interface FileStorageService {
    fun cleanSubmissionFiles(sub: ExtSubmission)

    fun persistSubmissionFiles(sub: ExtSubmission): ExtSubmission

    fun releaseSubmissionFiles(sub: ExtSubmission)
}
