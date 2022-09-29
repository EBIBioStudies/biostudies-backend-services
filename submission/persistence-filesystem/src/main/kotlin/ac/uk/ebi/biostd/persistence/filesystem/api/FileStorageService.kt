package ac.uk.ebi.biostd.persistence.filesystem.api

import ebi.ac.uk.extended.model.ExtSubmission

interface FileStorageService {
    fun cleanSubmissionFiles(sub: ExtSubmission)

    fun persistSubmissionFiles(sub: ExtSubmission): ExtSubmission

    fun releaseSubmissionFiles(sub: ExtSubmission)
}
