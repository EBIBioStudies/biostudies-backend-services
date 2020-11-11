package ac.uk.ebi.biostd.persistence.integration

import ac.uk.ebi.biostd.persistence.common.request.SaveSubmissionRequest
import ac.uk.ebi.biostd.persistence.common.service.PersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.model.DbAccessTag
import ac.uk.ebi.biostd.persistence.model.Sequence
import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepo
import ac.uk.ebi.biostd.persistence.repositories.LockExecutor
import ac.uk.ebi.biostd.persistence.repositories.SequenceDataRepository
import ac.uk.ebi.biostd.persistence.service.SubmissionSqlPersistenceService
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FileMode
import org.springframework.transaction.annotation.Transactional

@Suppress("TooManyFunctions")
internal open class SqlPersistenceService(
    private val submissionService: SubmissionSqlPersistenceService,
    private val sequenceRepository: SequenceDataRepository,
    private val accessTagsDataRepository: AccessTagDataRepo,
    private val submissionQueryService: SubmissionQueryService,
    private val lockExecutor: LockExecutor
) : PersistenceService {
    override fun sequenceAccNoPatternExists(pattern: String): Boolean = sequenceRepository.existsByPrefix(pattern)

    override fun createAccNoPatternSequence(pattern: String) {
        sequenceRepository.save(Sequence(pattern))
    }

    @Transactional
    override fun getSequenceNextValue(pattern: String): Long = lockExecutor.executeLocking(pattern) {
        val sequence = sequenceRepository.getByPrefix(pattern)
        var next = sequence.counter.count + 1
        while (submissionQueryService.existByAccNo("${sequence.prefix}$next")) next++

        sequence.counter.count = next
        sequenceRepository.save(sequence).counter.count
    }

    /**
     * Register the submission in the persistence state and latter process it. Note that both operations are executed
     * under db lock to guarantee single submission is saved and process at time.
     */
    @Transactional(readOnly = true)
    override fun saveAndProcessSubmissionRequest(saveRequest: SaveSubmissionRequest): ExtSubmission {
        val extended = saveSubmissionRequest(saveRequest)
        return processSubmission(SaveSubmissionRequest(extended, saveRequest.fileMode))
    }

    @Transactional(readOnly = true)
    override fun saveSubmissionRequest(saveRequest: SaveSubmissionRequest): ExtSubmission {
        val (sub, _, accNo) = saveRequest
        return lockExecutor.executeLocking(accNo) { submissionService.saveSubmissionRequest(sub) }
    }

    @Transactional(readOnly = true)
    override fun processSubmission(saveRequest: SaveSubmissionRequest): ExtSubmission {
        val (sub, mode, accNo) = saveRequest
        return lockExecutor.executeLocking(accNo) { submissionService.processSubmission(sub, mode) }
    }

    @Transactional
    override fun refreshSubmission(submission: ExtSubmission) {
        saveAndProcessSubmissionRequest(
            SaveSubmissionRequest(submission.copy(version = submission.version + 1), FileMode.MOVE))
    }

    override fun saveAccessTag(accessTag: String) {
        accessTagsDataRepository.save(DbAccessTag(name = accessTag))
    }

    override fun accessTagExists(accessTag: String) = accessTagsDataRepository.existsByName(accessTag)
}
