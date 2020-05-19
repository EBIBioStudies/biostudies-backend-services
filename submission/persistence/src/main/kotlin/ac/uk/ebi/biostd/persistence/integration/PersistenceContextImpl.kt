package ac.uk.ebi.biostd.persistence.integration

import ac.uk.ebi.biostd.persistence.mapping.extended.from.ToDbSubmissionMapper
import ac.uk.ebi.biostd.persistence.mapping.extended.to.ToExtSubmissionMapper
import ac.uk.ebi.biostd.persistence.model.DbAccessTag
import ac.uk.ebi.biostd.persistence.model.Sequence
import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepo
import ac.uk.ebi.biostd.persistence.repositories.LockExecutor
import ac.uk.ebi.biostd.persistence.repositories.SequenceDataRepository
import ac.uk.ebi.biostd.persistence.repositories.SubmissionDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserDataDataRepository
import ac.uk.ebi.biostd.persistence.service.filesystem.FileSystemService
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.model.User
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional

@Suppress("TooManyFunctions")
open class PersistenceContextImpl(
    private val subRepository: SubmissionDataRepository,
    private val sequenceRepository: SequenceDataRepository,
    private val accessTagsDataRepository: AccessTagDataRepo,
    private val lockExecutor: LockExecutor,
    private val userDataRepository: UserDataDataRepository,
    private val toDbSubmissionMapper: ToDbSubmissionMapper,
    private val toExtSubmissionMapper: ToExtSubmissionMapper,
    private val systemService: FileSystemService
) : PersistenceContext {
    override fun sequenceAccNoPatternExists(pattern: String): Boolean = sequenceRepository.existsByPrefix(pattern)

    override fun createAccNoPatternSequence(pattern: String) {
        sequenceRepository.save(Sequence(pattern))
    }

    @Transactional
    override fun getSequenceNextValue(pattern: String): Long {
        return lockExecutor.executeLocking(pattern) {
            val sequence = sequenceRepository.getByPrefix(pattern)
            sequence.counter.count = sequence.counter.count + 1
            sequenceRepository.save(sequence).counter.count
        }
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    override fun saveSubmission(saveRequest: SaveRequest): ExtSubmission {
        val (submission, mode) = saveRequest
        return lockExecutor.executeLocking(submission.accNo) { saveSubmission(submission, mode) }
    }

    @Transactional
    override fun refreshSubmission(submission: ExtSubmission, submitter: User) {
        saveSubmission(SaveRequest(submission.copy(version = submission.version + 1), FileMode.MOVE))
    }

    private fun saveSubmission(submission: ExtSubmission, mode: FileMode): ExtSubmission {
        subRepository.expireActiveVersions(submission.accNo)
        deleteSubmissionDrafts(submission.submitter, submission.accNo)
        deleteSubmissionDrafts(submission.owner, submission.accNo)
        return saveNewVersion(submission, mode)
    }

    private fun saveNewVersion(submission: ExtSubmission, mode: FileMode): ExtSubmission {
        val dbSubmission = subRepository.save(toDbSubmissionMapper.toSubmissionDb(submission))
        systemService.persistSubmissionFiles(submission, mode)
        return toExtSubmissionMapper.toExtSubmission(dbSubmission)
    }

    override fun deleteSubmissionDrafts(userEmail: String, accNo: String) {
        userDataRepository.deleteByUserEmailAndKeyIgnoreCaseContaining(userEmail, accNo)
    }

    override fun getNextVersion(accNo: String): Int = (subRepository.getLastVersion(accNo) ?: 0) + 1

    override fun saveAccessTag(accessTag: String) {
        accessTagsDataRepository.save(DbAccessTag(name = accessTag))
    }

    override fun accessTagExists(accessTag: String) = accessTagsDataRepository.existsByName(accessTag)
}
