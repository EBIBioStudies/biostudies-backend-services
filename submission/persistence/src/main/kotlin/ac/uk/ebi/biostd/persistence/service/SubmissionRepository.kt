package ac.uk.ebi.biostd.persistence.service

import ac.uk.ebi.biostd.persistence.mapping.ext.extensions.toExtSubmission
import ac.uk.ebi.biostd.persistence.repositories.SubmissionDataRepository
import ebi.ac.uk.extended.model.ExtSubmission

class SubmissionRepository(
    private val submissionRepository: SubmissionDataRepository,
    private val subFileResolver: SubFileResolver
) {

    fun getByAccNo(accNo: String): ExtSubmission {
        val fileSource = subFileResolver.getSource(accNo)
        val submission = submissionRepository.getByAccNoAndVersionGreaterThan(accNo)
        return submission.toExtSubmission(fileSource)
    }

    fun expireSubmission(accNo: String): Boolean {
        val submission = submissionRepository.findByAccNoAndVersionGreaterThan(accNo)
        return when {
            submission == null -> false
            else -> {
                submission.version = -submission.version
                submissionRepository.save(submission)
                return true
            }
        }
    }
}
