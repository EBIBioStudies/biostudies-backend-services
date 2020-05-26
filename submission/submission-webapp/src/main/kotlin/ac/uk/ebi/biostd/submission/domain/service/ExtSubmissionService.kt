package ac.uk.ebi.biostd.submission.domain.service

import ac.uk.ebi.biostd.persistence.service.SubmissionRepository
import ebi.ac.uk.extended.model.ExtSubmission

class ExtSubmissionService(private val submissionRepository: SubmissionRepository) {
    fun getExtendedSubmission(accNo: String): ExtSubmission {
        // TODO validate only superusers can use it
        return submissionRepository.getExtByAccNo(accNo)
    }

    fun submitExtendedSubmission(extSubmission: ExtSubmission): ExtSubmission {
        return extSubmission
    }
}
