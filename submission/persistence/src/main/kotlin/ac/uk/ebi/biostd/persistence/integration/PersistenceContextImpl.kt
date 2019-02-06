package ac.uk.ebi.biostd.persistence.integration

import ac.uk.ebi.biostd.persistence.mapping.SubmissionDbMapper
import ac.uk.ebi.biostd.persistence.mapping.SubmissionMapper
import ac.uk.ebi.biostd.persistence.model.AccessTag
import ac.uk.ebi.biostd.persistence.repositories.SequenceDataRepository
import ac.uk.ebi.biostd.persistence.repositories.SubmissionDataRepository
import arrow.core.getOrElse
import arrow.core.toOption
import ebi.ac.uk.base.toOption
import ebi.ac.uk.model.AccPattern
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.User
import ebi.ac.uk.model.constants.SubFields
import ebi.ac.uk.model.constants.SubFields.ATTACH_TO
import ebi.ac.uk.persistence.PersistenceContext

class PersistenceContextImpl(
    private val subRepository: SubmissionDataRepository,
    private val sequenceRepository: SequenceDataRepository,
    private val subDbMapper: SubmissionDbMapper,
    private val subMapper: SubmissionMapper
) : PersistenceContext {

    // TODO make this thread safe!
    override fun getSequenceNextValue(pattern: AccPattern) =
        sequenceRepository.save(
            sequenceRepository.getByPrefixAndSuffix(pattern.prefix, pattern.postfix).apply { counter++ }).id

    override fun getParentAccessTags(submission: Submission) =
        getParentSubmission(submission)
            .map { it.accessTags }.getOrElse { emptyList<AccessTag>() }
            .map { it.name }

    override fun getParentAccPattern(submission: Submission) =
        getParentSubmission(submission)
            .flatMap { parent ->
                parent.attributes.firstOrNull { it.name == SubFields.ACC_NO_TEMPLATE.toString() }.toOption() }
            .map { it.value }

    override fun getSubmission(accNo: String) =
        subRepository.findByAccNoAndVersionGreaterThan(accNo).map { subDbMapper.toExtSubmission(it) }.toOption()

    override fun saveSubmission(submission: ExtendedSubmission) {
        setSubmissionVersion(submission)
        subRepository.save(subMapper.toSubmissionDb(submission))
    }

    override fun canUserProvideAccNo(user: User): Boolean {
        return true
    }

    override fun canSubmit(accNo: String, user: User): Boolean {
        return true
    }

    override fun isNew(submission: ExtendedSubmission) = subRepository.existsByAccNo(submission.accNo)

    private fun getParentSubmission(submission: Submission) =
        submission.find(ATTACH_TO).toOption().map { subRepository.getByAccNoAndVersionGreaterThan(it) }

    private fun setSubmissionVersion(submission: ExtendedSubmission) =
        subRepository.findByAccNoAndVersionGreaterThan(submission.accNo).ifPresent {
            submission.version = it.version + 1
            it.version = -it.version
            subRepository.save(it)
        }
}
