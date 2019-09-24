package ac.uk.ebi.biostd.persistence.service

import ac.uk.ebi.biostd.persistence.mapping.SubmissionDbMapper
import ac.uk.ebi.biostd.persistence.model.Submission
import ac.uk.ebi.biostd.persistence.model.User
import ac.uk.ebi.biostd.persistence.repositories.SubmissionDataRepository
import ac.uk.ebi.biostd.persistence.util.OffsetLimitPageable
import ac.uk.ebi.biostd.persistence.util.SubmissionFilter
import antlr.StringUtils
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
        var filterSpec: Specification<Submission> = Specification.where(
            withUser(userId)).and(withVersionGreaterThan(0))

        filter.accNo?.let { filterSpec = filterSpec.and(withAccession(it)) }
        filter.rTimeFrom?.let { filterSpec = filterSpec.and(withFrom(it)) }
        filter.rTimeTo?.let { filterSpec = filterSpec.and(withTo(it)) }
        filter.keywords?.let {
            if (filter.keywords.isNotBlank())
                filterSpec = filterSpec.and(withTitleLike(it))
        }

        return submissionRepository.findAll(filterSpec, OffsetLimitPageable(filter.offset,
            filter.limit, Sort.by("releaseTime").descending())).getContent()
    }

    private fun withVersionGreaterThan(version: Int): Specification<Submission> {
        return Specification{ root, query, cb  ->
            cb.greaterThan<Int>(root.get<Int>("version"), version)}
    }

    private fun withAccession(accNo: String): Specification<Submission> {
        return Specification{ root, query, cb ->
            cb.equal(root.get<Int>("accNo"), accNo) }
    }

    private fun withTitleLike(title: String): Specification<Submission> {
        return Specification{ root, query, cb ->
            cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%") }
    }

    private fun withUser(userId: Long): Specification<Submission> {
        return Specification{ root, query, cb ->
            cb.equal(root.get<User>("owner").get<Long>("id"), userId) }
    }

    private fun withFrom(from: Long): Specification<Submission> {
        return Specification{ root, query, cb ->
            cb.greaterThan(root.get("releaseTime"), from) }
    }

    private fun withTo(to: Long): Specification<Submission> {
        return Specification{ root, query, cb ->
            cb.lessThan(root.get("releaseTime"), to) }
    }
}
