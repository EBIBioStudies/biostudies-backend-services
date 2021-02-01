package ac.uk.ebi.biostd.persistence.doc.db.data

import ac.uk.ebi.biostd.persistence.doc.db.repositories.SubmissionRequestRepository
import ac.uk.ebi.biostd.persistence.doc.model.SubmissionRequest

class SubmissionRequestDocDataRepository(private val submissionRequestRepository: SubmissionRequestRepository) {
    fun saveRequest(submissionRequest: SubmissionRequest) {
        submissionRequestRepository.save(submissionRequest)
    }
}
