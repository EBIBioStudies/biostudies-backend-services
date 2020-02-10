package ac.uk.ebi.biostd.persistence.integration

import ac.uk.ebi.biostd.persistence.exception.ProjectNotFoundException
import ac.uk.ebi.biostd.persistence.mapping.extended.from.ToDbSubmissionMapper
import ac.uk.ebi.biostd.persistence.mapping.extended.to.ToExtSubmissionMapper
import ac.uk.ebi.biostd.persistence.model.AccessTag
import ac.uk.ebi.biostd.persistence.model.Sequence
import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepo
import ac.uk.ebi.biostd.persistence.repositories.LockExecutor
import ac.uk.ebi.biostd.persistence.repositories.SequenceDataRepository
import ac.uk.ebi.biostd.persistence.repositories.SubmissionDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserDataDataRepository
import ac.uk.ebi.biostd.persistence.service.FilePersistenceService
import arrow.core.toOption
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.model.constants.SubFields.ACC_NO_TEMPLATE
import ebi.ac.uk.util.collections.ifNotEmpty
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

@Suppress("TooManyFunctions")
open class PersistenceContextImpl(
    private val subRepository: SubmissionDataRepository,
    private val sequenceRepository: SequenceDataRepository,
    private val accessTagsDataRepository: AccessTagDataRepo,
    private val lockExecutor: LockExecutor,
    private val userDataRepository: UserDataDataRepository,
    private val toDbSubmissionMapper: ToDbSubmissionMapper,
    private val toExtSubmissionMapper: ToExtSubmissionMapper,
    private val filePersistenceService: FilePersistenceService
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

    override fun getParentAccPattern(parentAccNo: String) =
        getParentSubmission(parentAccNo)
            .toOption()
            .flatMap { parent -> parent.attributes.firstOrNull { it.name == ACC_NO_TEMPLATE.value }.toOption() }
            .map { it.value }

    private fun getSubmission(accNo: String) = subRepository.findByAccNoAndVersionGreaterThan(accNo)

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    override fun saveSubmission(submission: ExtSubmission, submitter: String, submitterId: Long): ExtSubmission {
        return lockExecutor.executeLocking(submission.accNo) {
            subRepository.expireActiveVersions(submission.accNo)
            val dbSubmission = toDbSubmissionMapper.toSubmissionDb(submission, submitter)
            val persistedSubmission = toExtSubmissionMapper.toExtSubmission(subRepository.save(dbSubmission))
            filePersistenceService.persistSubmissionFiles(submission)
            deleteSubmissionDrafts(submitterId, submission.accNo)
            persistedSubmission
        }
    }

    override fun getAuthor(accNo: String): String {
        return getSubmission(accNo)!!.owner.email
    }

    override fun deleteSubmissionDrafts(userId: Long, accNo: String) {
        userDataRepository.findByUserIdAndKeyIgnoreCaseContaining(userId, accNo).ifNotEmpty {
            userDataRepository.deleteAll(it)
        }
    }

    override fun getSecret(accNo: String): String = getSubmission(accNo)!!.secretKey

    override fun getAccessTags(accNo: String): List<String> {
        return subRepository.getByAccNoAndVersionGreaterThan(accNo, 0).accessTags.map { it.name }
    }

    override fun getReleaseTime(accNo: String): OffsetDateTime? {
        return subRepository.getByAccNoAndVersionGreaterThan(accNo, 0).releaseTime
    }

    override fun findCreationTime(accNo: String): OffsetDateTime? {
        return subRepository.findByAccNoAndVersionGreaterThan(accNo, 0)?.creationTime
    }

    override fun existByAccNo(accNo: String): Boolean {
        return subRepository.existsByAccNo(accNo)
    }

    override fun getNextVersion(accNo: String): Int = (subRepository.getLastVersion(accNo) ?: 0) + 1

    override fun saveAccessTag(accessTag: String) {
        accessTagsDataRepository.save(AccessTag(name = accessTag))
    }

    override fun accessTagExists(accessTag: String) = accessTagsDataRepository.existsByName(accessTag)

    override fun isNew(accNo: String) = subRepository.existsByAccNo(accNo).not()

    private fun getParentSubmission(parentAccNo: String) =
        subRepository.findByAccNoAndVersionGreaterThan(parentAccNo) ?: throw ProjectNotFoundException(parentAccNo)
}
