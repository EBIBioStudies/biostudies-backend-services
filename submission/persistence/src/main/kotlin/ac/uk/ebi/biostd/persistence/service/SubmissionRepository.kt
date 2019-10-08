package ac.uk.ebi.biostd.persistence.service

import ac.uk.ebi.biostd.persistence.common.SubmissionTypes.Project
import ac.uk.ebi.biostd.persistence.mapping.SubmissionDbMapper
import ac.uk.ebi.biostd.persistence.model.AccessTag
import ac.uk.ebi.biostd.persistence.repositories.SubmissionDataRepository
import ac.uk.ebi.biostd.persistence.util.SubmissionFilter
import ac.uk.ebi.biostd.persistence.util.SubmissionFilterSpecification
import ebi.ac.uk.model.ExtendedSubmission
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

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

    fun findProjectsByAccessTags(tags: List<AccessTag>) =
        submissionRepository.findByTypeAndAccNo(Project.value, tags.map { it.name })
            .map { submissionDbMapper.toSubmission(it) }

    fun getSubmissionsByUser(userId: Long, filter: SubmissionFilter): List<ExtendedSubmission> {
        val filterSpecs = SubmissionFilterSpecification(userId, filter)
        val pageable = PageRequest.of(filter.pageNumber, filter.limit, Sort.by("releaseTime").descending())

        return submissionRepository
            .findAll(filterSpecs.specification, pageable)
            .content
            .map { submissionDbMapper.toExtSubmission(it) }
    }
}
