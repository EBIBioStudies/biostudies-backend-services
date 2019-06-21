package ac.uk.ebi.biostd.persistence.service

import ac.uk.ebi.biostd.persistence.mapping.ext.to.toExtSubmission
import ac.uk.ebi.biostd.persistence.repositories.SubmissionDataRepository
import ebi.ac.uk.extended.model.ExtSubmission

class SubmissionRepository(
    private val submissionRepository: SubmissionDataRepository,
    private val subFileResolver: SubFileResolver
) {

    fun getByAccNo(accNo: String): ExtSubmission {
        val fileSource = subFileResolver.getSubmissionSource(accNo)
        val submission = submissionRepository.getByAccNoAndVersionGreaterThan(accNo)
        return submission.toExtSubmission(fileSource)
    }

    fun expireSubmission(accNo: String): Boolean {
        return when (val submission = submissionRepository.findByAccNoAndVersionGreaterThan(accNo)) {
            null -> false
            else -> {
                submission.version = -submission.version
                submissionRepository.save(submission)
                return true
            }
        }
    }
}
