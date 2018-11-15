package ac.uk.ebi.biostd.persistence.integration

import ac.uk.ebi.biostd.persistence.mapping.SubmissionDbMapper
import ac.uk.ebi.biostd.persistence.mapping.SubmissionMapper
import ac.uk.ebi.biostd.persistence.model.AccessTag
import ac.uk.ebi.biostd.persistence.repositories.SubmissionDataRepository
import arrow.core.getOrElse
import arrow.core.toOption
import ebi.ac.uk.base.toOption
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.User
import ebi.ac.uk.model.constans.SubFields
import ebi.ac.uk.model.constans.SubFields.ATTACH_TO
import ebi.ac.uk.persistence.PersistenceContext

class PersistenceContextImpl(
    private val subRepository: SubmissionDataRepository,
    private val subDbMapper: SubmissionDbMapper,
    private val subMapper: SubmissionMapper
) : PersistenceContext {

    override fun getSequenceNextValue(name: String): Long {
        TODO("not implemented")
    }

    override fun getParentAccessTags(submission: Submission) =
        getParentSubmission(submission)
            .map { it.accessTags }.getOrElse { emptyList<AccessTag>() }
            .map { it.name }

    override fun getParentAccPattern(submission: Submission) =
        getParentSubmission(submission)
            .flatMap { parent -> parent.attributes.firstOrNull { it.name == SubFields.ACC_NO_TEMPLATE.toString() }.toOption() }
            .map { it.value }

    private fun getParentSubmission(submission: Submission) =
        submission.find(ATTACH_TO).toOption().map { subRepository.getByAccNoAndVersionGreaterThan(it) }

    override fun getSubmission(accNo: String) =
        subRepository.findByAccNoAndVersionGreaterThan(accNo).map { subDbMapper.toExtSubmission(it) }.toOption()

    override fun saveSubmission(submission: ExtendedSubmission) {
        subRepository.save(subMapper.toSubmissionDb(submission))
    }

    override fun canUserProvideAccNo(user: User): Boolean {
        return true
    }

    override fun canSubmit(accNo: String, user: User): Boolean {
        return true
    }
}