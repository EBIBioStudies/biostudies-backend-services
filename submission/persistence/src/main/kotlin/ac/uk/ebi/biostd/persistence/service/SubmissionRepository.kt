package ac.uk.ebi.biostd.persistence.service

import ac.uk.ebi.biostd.persistence.mapping.SubmissionDbMapper
import ac.uk.ebi.biostd.persistence.model.AccessTag
import ac.uk.ebi.biostd.persistence.repositories.SubmissionDataRepository

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

    fun findSubmissionsByAccessTags(accessTags: List<AccessTag>) =
        submissionRepository.findDistinctByRootSectionTypeAndAccessTagsInAndVersionGreaterThan(
            Companion.PROJECT_TYPE, accessTags, 0).map { submissionDbMapper.toSubmission(it) }

    companion object {
        const val PROJECT_TYPE = "Project" //TODO: refactor to enum when submission type is handled
    }

}
