package ac.uk.ebi.biostd.persistence.integration

import ac.uk.ebi.biostd.persistence.exception.ProjectNotFoundException
import ac.uk.ebi.biostd.persistence.mapping.SubmissionDbMapper
import ac.uk.ebi.biostd.persistence.mapping.SubmissionMapper
import ac.uk.ebi.biostd.persistence.model.AccessTag
import ac.uk.ebi.biostd.persistence.model.Sequence
import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepository
import ac.uk.ebi.biostd.persistence.repositories.LockExecutor
import ac.uk.ebi.biostd.persistence.repositories.SequenceDataRepository
import ac.uk.ebi.biostd.persistence.repositories.SubmissionDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserDataDataRepository
import arrow.core.getOrElse
import arrow.core.toOption
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constants.SubFields.ACC_NO_TEMPLATE
import ebi.ac.uk.model.extensions.attachTo
import ebi.ac.uk.persistence.PersistenceContext
import ebi.ac.uk.util.collections.ifNotEmpty
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional

@Suppress("TooManyFunctions")
open class PersistenceContextImpl(
    private val subRepository: SubmissionDataRepository,
    private val sequenceRepository: SequenceDataRepository,
    private val accessTagsDataRepository: AccessTagDataRepository,
    private val lockExecutor: LockExecutor,
    private val subDbMapper: SubmissionDbMapper,
    private val subMapper: SubmissionMapper,
    private val userDataRepository: UserDataDataRepository
) : PersistenceContext {
    override fun createAccNoPatternSequence(pattern: String) {
        sequenceRepository.save(Sequence(pattern))
    }

    @Transactional
    override fun getSequenceNextValue(pattern: String): Long {
        val sequence = sequenceRepository.getByPrefix(pattern)
        sequence.counter.count = sequence.counter.count + 1
        sequenceRepository.save(sequence)
        return sequence.counter.count
    }

    override fun getParentAccessTags(submission: Submission): List<String> =
        getParentSubmission(submission)
            .map { it.accessTags }
            .getOrElse { emptyList<AccessTag>() }
            .map { it.name }

    override fun getParentAccPattern(submission: Submission) =
        getParentSubmission(submission)
            .flatMap { parent -> parent.attributes.firstOrNull { it.name == ACC_NO_TEMPLATE.value }.toOption() }
            .map { it.value }

    override fun getSubmission(accNo: String) =
        subRepository.findByAccNoAndVersionGreaterThan(accNo)?.let { subDbMapper.toExtSubmission(it) }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    override fun saveSubmission(submission: ExtendedSubmission): Submission {
        return lockExecutor.executeLocking(submission.accNo) {
            val nextVersion = (subRepository.getLastVersion(submission.accNo) ?: 0) + 1
            subRepository.expireActiveVersions(submission.accNo)
            submission.version = nextVersion
            subDbMapper.toSubmission(subRepository.save(subMapper.toSubmissionDb(submission)))
        }
    }

    override fun deleteSubmissionDrafts(submission: ExtendedSubmission) {
        userDataRepository.findByUserIdAndKeyIgnoreCaseContaining(submission.user.id, submission.accNo).ifNotEmpty {
            userDataRepository.deleteAll(it)
        }
    }

    override fun getSecret(accNo: String): String = getSubmission(accNo)!!.secretKey

    override fun saveAccessTag(accessTag: String) {
        accessTagsDataRepository.save(AccessTag(name = accessTag))
    }

    override fun accessTagExists(accessTag: String) = accessTagsDataRepository.existsByName(accessTag)

    override fun isNew(accNo: String) = subRepository.existsByAccNo(accNo).not()

    private fun getParentSubmission(submission: Submission) =
        submission.attachTo?.let {
            subRepository.findByAccNoAndVersionGreaterThan(it) ?: throw ProjectNotFoundException(it)
        }.toOption()
}
