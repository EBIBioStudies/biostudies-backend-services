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
import ac.uk.ebi.biostd.persistence.service.FilePersistenceService
import ebi.ac.uk.extended.model.ExtSubmission
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional

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
    override fun saveSubmission(submission: ExtSubmission, submitter: String, submitterId: Long): ExtSubmission {
        return lockExecutor.executeLocking(submission.accNo) {
            subRepository.expireActiveVersions(submission.accNo)
            deleteSubmissionDrafts(submitterId, submission.accNo)

            val newSubmission = saveNewVersion(submission, submitter)
            filePersistenceService.persistSubmissionFiles(submission)
            newSubmission
        }
    }

    private fun saveNewVersion(submission: ExtSubmission, submitter: String): ExtSubmission {
        val dbSubmission = toDbSubmissionMapper.toSubmissionDb(submission, submitter)
        return toExtSubmissionMapper.toExtSubmission(subRepository.save(dbSubmission))
    }

    override fun deleteSubmissionDrafts(userId: Long, accNo: String) {
        userDataRepository.deleteByUserIdAndKeyIgnoreCaseContaining(userId, accNo)
    }

    override fun getNextVersion(accNo: String): Int = (subRepository.getLastVersion(accNo) ?: 0) + 1

    override fun saveAccessTag(accessTag: String) {
        accessTagsDataRepository.save(DbAccessTag(name = accessTag))
    }

    override fun accessTagExists(accessTag: String) = accessTagsDataRepository.existsByName(accessTag)
}
