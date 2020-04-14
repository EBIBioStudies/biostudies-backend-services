package ac.uk.ebi.biostd.persistence.service

import ac.uk.ebi.biostd.persistence.exception.SubmissionNotFoundException
import ac.uk.ebi.biostd.persistence.filter.SubmissionFilter
import ac.uk.ebi.biostd.persistence.filter.SubmissionFilterSpecification
import ac.uk.ebi.biostd.persistence.mapping.SubmissionDbMapper
import ac.uk.ebi.biostd.persistence.projections.SimpleSubmission
import ac.uk.ebi.biostd.persistence.projections.SimpleSubmission.Companion.asSimpleSubmission
import ac.uk.ebi.biostd.persistence.repositories.SubmissionDataRepository
import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraphs
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.Submission
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

class SubmissionRepository(
    private val submissionRepository: SubmissionDataRepository,
    private val submissionDbMapper: SubmissionDbMapper
) {
    fun getByAccNo(accNo: String): Submission = submissionDbMapper.toSubmission(getSubmission(accNo))

    fun getExtendedByAccNo(accNo: String): ExtendedSubmission = submissionDbMapper.toExtSubmission(getSubmission(accNo))

    fun getExtendedLastVersionByAccNo(accNo: String): ExtendedSubmission =
        submissionDbMapper.toExtSubmission(submissionRepository.getFirstByAccNoOrderByVersionDesc(accNo))

    fun expireSubmission(accNo: String) {
        submissionRepository.findByAccNoAndVersionGreaterThan(accNo)?.let {
            it.version = -it.version
            submissionRepository.save(it)
        }
    }

    fun getSubmissionsByUser(userId: Long, filter: SubmissionFilter): List<SimpleSubmission> {
        val filterSpecs = SubmissionFilterSpecification(userId, filter)
        val pageable = PageRequest.of(filter.pageNumber, filter.limit, Sort.by("releaseTime").descending())
        return submissionRepository
            .findAll(filterSpecs.specification, pageable, EntityGraphs.named(SimpleSubmission.SIMPLE_GRAPH))
            .content
            .map { it.asSimpleSubmission() }
    }

    private fun getSubmission(accNo: String) =
        submissionRepository.getByAccNoAndVersionGreaterThan(accNo) ?: throw SubmissionNotFoundException(accNo)
}
