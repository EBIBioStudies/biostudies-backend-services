package ac.uk.ebi.biostd.persistence.service

import ac.uk.ebi.biostd.persistence.mapping.SubmissionDbMapper
import ac.uk.ebi.biostd.persistence.model.Submission
import ac.uk.ebi.biostd.persistence.model.User
import ac.uk.ebi.biostd.persistence.repositories.SubmissionDataRepository
import ac.uk.ebi.biostd.persistence.util.OffsetLimitPageable
import ac.uk.ebi.biostd.persistence.util.SubmissionFilter
import ac.uk.ebi.biostd.persistence.util.SubmissionFilterSpecification
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification

class SubmissionRepository(
    private val submissionRepository: SubmissionDataRepository,
    private val submissionDbMapper: SubmissionDbMapper
) {
    fun getByAccNo(accNo: String) =
        submissionDbMapper.toSubmission(submissionRepository.getByAccNoAndVersionGreaterThan(accNo))

    fun getExtendedByAccNo(accNo: String) =
        submissionDbMapper.toExtSubmission(submissionRepository.getByAccNoAndVersionGreaterThan(accNo))

    fun getExtendedLastVersionByAccNo(accNo: String) =
        submissionDbMapper.toExtSubmission(submissionRepository.getFirstByAccNoOrderByVersionDesc(accNo))

    fun expireSubmission(accNo: String) {
        submissionRepository.findByAccNoAndVersionGreaterThan(accNo)?.let {
            it.version = -it.version
            submissionRepository.save(it)
        }
    }

    fun getSubmissionsByUser(userId: Long, filter: SubmissionFilter): List<Submission> {
        var filterSpecs = SubmissionFilterSpecification(userId, filter)
        return submissionRepository.findAll(filterSpecs.specification, OffsetLimitPageable(filter.offset,
            filter.limit, Sort.by("releaseTime").descending())).getContent()
    }


}
