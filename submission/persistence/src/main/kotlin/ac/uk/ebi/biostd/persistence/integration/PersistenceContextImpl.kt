package ac.uk.ebi.biostd.persistence.integration

import ac.uk.ebi.biostd.persistence.exception.ProjectNotFoundException
import ac.uk.ebi.biostd.persistence.mapping.SubmissionDbMapper
import ac.uk.ebi.biostd.persistence.mapping.SubmissionMapper
import ac.uk.ebi.biostd.persistence.mapping.extended.from.ToDbSubmissionMapper
import ac.uk.ebi.biostd.persistence.mapping.extended.to.ToExtSubmissionMapper
import ac.uk.ebi.biostd.persistence.model.AccessTag
import ac.uk.ebi.biostd.persistence.model.Sequence
import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepository
import ac.uk.ebi.biostd.persistence.repositories.LockExecutor
import ac.uk.ebi.biostd.persistence.repositories.SequenceDataRepository
import ac.uk.ebi.biostd.persistence.repositories.SubmissionDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserDataDataRepository
import arrow.core.getOrElse
import arrow.core.toOption
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constants.SubFields.ACC_NO_TEMPLATE
import ebi.ac.uk.model.extensions.attachTo
import ebi.ac.uk.util.collections.ifNotEmpty
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

@Suppress("TooManyFunctions")
open class PersistenceContextImpl(
    private val subRepository: SubmissionDataRepository,
    private val sequenceRepository: SequenceDataRepository,
    private val accessTagsDataRepository: AccessTagDataRepository,
    private val lockExecutor: LockExecutor,
    private val subDbMapper: SubmissionDbMapper,
    private val subMapper: SubmissionMapper,
    private val userDataRepository: UserDataDataRepository,
    private val toDbSubmissionMapper: ToDbSubmissionMapper,
    private val toExtSubmissionMapper: ToExtSubmissionMapper
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

    override fun hasParent(submission: ExtendedSubmission): Boolean = getParentSubmission(submission).isDefined()

    override fun getParentAccessTags(submission: Submission): List<String> =
        getParentSubmission(submission)
            .map { it.accessTags }
            .getOrElse { emptyList<AccessTag>() }
            .map { it.name }

    override fun getParentAccPattern(parentAccNo: String) =
        getParentSubmission(parentAccNo)
            .toOption()
            .flatMap { parent -> parent.attributes.firstOrNull { it.name == ACC_NO_TEMPLATE.value }.toOption() }
            .map { it.value }

    override fun getParentReleaseTime(submission: Submission): OffsetDateTime? =
        getParentSubmission(submission).map { parent -> parent.releaseTime }.orNull()

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

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    override fun saveSubmission(submission: ExtSubmission, submitter: String): ExtSubmission {
        return lockExecutor.executeLocking(submission.accNo) {
            subRepository.expireActiveVersions(submission.accNo)
            val dbSubmission = toDbSubmissionMapper.toSubmissionDb(submission, submitter)
            toExtSubmissionMapper.toExtSubmission(subRepository.save(dbSubmission))

            // TODO: Move files and generate output files
        }
    }

    override fun deleteSubmissionDrafts(userId: Long, accNo: String) {
        userDataRepository.findByUserIdAndKeyIgnoreCaseContaining(userId, accNo).ifNotEmpty {
            userDataRepository.deleteAll(it)
        }
    }

    override fun getSecret(accNo: String): String = getSubmission(accNo)!!.secretKey

    override fun getAccessTags(attachTo: String): List<String> {
        return subRepository.getByAccNoAndVersionGreaterThan(attachTo, 0).accessTags.map { it.name }
    }

    override fun getReleaseTime(attachTo: String): OffsetDateTime? {
        return subRepository.getByAccNoAndVersionGreaterThan(attachTo, 0).releaseTime
    }

    override fun existByAccNo(attachTo: String): Boolean {
        return subRepository.existsByAccNo(attachTo)
    }

    override fun getNextVersion(accNo: String): Int = (subRepository.getLastVersion(accNo) ?: 0) + 1

    override fun saveAccessTag(accessTag: String) {
        accessTagsDataRepository.save(AccessTag(name = accessTag))
    }

    override fun accessTagExists(accessTag: String) = accessTagsDataRepository.existsByName(accessTag)

    override fun isNew(accNo: String) = subRepository.existsByAccNo(accNo).not()

    private fun getParentSubmission(parentAccNo: String) =
        subRepository.findByAccNoAndVersionGreaterThan(parentAccNo) ?: throw ProjectNotFoundException(parentAccNo)

    private fun getParentSubmission(submission: Submission) =
        submission.attachTo?.let {
            subRepository.findByAccNoAndVersionGreaterThan(it) ?: throw ProjectNotFoundException(it)
        }.toOption()
}
