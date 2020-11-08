package ac.uk.ebi.biostd.persistence.common.service

import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FileMode

interface SubmissionPersistenceService {

    fun saveSubmissionRequest(submission: ExtSubmission): ExtSubmission
    fun processSubmission(submission: ExtSubmission, mode: FileMode): ExtSubmission
}
