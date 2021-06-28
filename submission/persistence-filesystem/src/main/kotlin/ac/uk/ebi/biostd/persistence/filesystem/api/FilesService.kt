package ac.uk.ebi.biostd.persistence.filesystem.api

import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FileMode

interface FilesService {
    fun persistSubmissionFiles(submission: ExtSubmission, mode: FileMode): ExtSubmission
}
