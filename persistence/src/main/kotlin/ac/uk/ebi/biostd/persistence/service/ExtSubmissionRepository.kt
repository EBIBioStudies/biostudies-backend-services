package ac.uk.ebi.biostd.persistence.service

import ac.uk.ebi.biostd.persistence.mapping.SubmissionDbMapper
import ac.uk.ebi.biostd.persistence.repositories.SubmissionDataRepository

class ExtSubmissionRepository(
    private val submissionRepository: SubmissionDataRepository,
    private val submissionDbMapper: SubmissionDbMapper
) {

    fun findByAccNo(accNo: String) =
            submissionDbMapper.toExtSubmission(submissionRepository.findByAccNoAndVersionGreaterThan(accNo))
}
