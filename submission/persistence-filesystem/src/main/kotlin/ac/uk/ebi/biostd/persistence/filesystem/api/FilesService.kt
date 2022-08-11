package ac.uk.ebi.biostd.persistence.filesystem.api

import ebi.ac.uk.extended.model.ExtSubmission

interface FilesService {
    fun persistSubmissionFiles(sub: ExtSubmission): ExtSubmission

    fun cleanSubmissionFiles(sub: ExtSubmission)
}
