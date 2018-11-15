package ac.uk.ebi.biostd.persistence.service

import ac.uk.ebi.biostd.persistence.mapping.SubmissionDbMapper
import ac.uk.ebi.biostd.persistence.repositories.SubmissionDataRepository

class SubmissionRepository(
    private val submissionRepository: SubmissionDataRepository,
    private val submissionDbMapper: SubmissionDbMapper
) {

    fun findByAccNo(accNo: String) =
        submissionDbMapper.toSubmission(submissionRepository.getByAccNoAndVersionGreaterThan(accNo))
}
