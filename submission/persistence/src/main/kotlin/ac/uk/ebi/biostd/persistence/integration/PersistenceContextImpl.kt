package ac.uk.ebi.biostd.persistence.integration

import ac.uk.ebi.biostd.persistence.mapping.extended.from.ToDbSubmissionMapper
import ac.uk.ebi.biostd.persistence.model.DbAccessTag
import ac.uk.ebi.biostd.persistence.model.Sequence
import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepo
import ac.uk.ebi.biostd.persistence.repositories.LockExecutor
import ac.uk.ebi.biostd.persistence.repositories.SequenceDataRepository
import ac.uk.ebi.biostd.persistence.service.SubmissionPersistenceService
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FileMode
import ebi.ac.uk.model.User
import org.springframework.transaction.annotation.Transactional

@Suppress("TooManyFunctions")
open class PersistenceContextImpl(
    private val submissionService: SubmissionPersistenceService,
    private val sequenceRepository: SequenceDataRepository,
    private val accessTagsDataRepository: AccessTagDataRepo,
    private val lockExecutor: LockExecutor,
    private val toDbMapper: ToDbSubmissionMapper
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

    /**
     * Register the submission in the persistence state and latter process it. Note that both operations are executed
     * under db lock to guarantee single submission is saved and process at time.
     */
    @Transactional(readOnly = true)
    override fun saveAndProcessSubmissionRequest(saveRequest: SaveRequest): ExtSubmission {
        saveSubmissionRequest(saveRequest)
        return processSubmission(saveRequest)
    }

    @Transactional(readOnly = true)
    override fun saveSubmissionRequest(saveRequest: SaveRequest) {
        val (sub, _, accNo) = saveRequest
        lockExecutor.executeLocking(accNo) { submissionService.saveSubmission(toDbMapper.toSubmissionDb(sub)) }
    }

    @Transactional(readOnly = true)
    override fun processSubmission(saveRequest: SaveRequest): ExtSubmission {
        val (sub, mode, accNo) = saveRequest
        return lockExecutor.executeLocking(accNo) { submissionService.processSubmission(sub, mode) }
    }

    @Transactional
    override fun refreshSubmission(submission: ExtSubmission, submitter: User) {
        saveAndProcessSubmissionRequest(SaveRequest(submission.copy(version = submission.version + 1), FileMode.MOVE))
    }

    override fun saveAccessTag(accessTag: String) {
        accessTagsDataRepository.save(DbAccessTag(name = accessTag))
    }

    override fun accessTagExists(accessTag: String) = accessTagsDataRepository.existsByName(accessTag)
}
