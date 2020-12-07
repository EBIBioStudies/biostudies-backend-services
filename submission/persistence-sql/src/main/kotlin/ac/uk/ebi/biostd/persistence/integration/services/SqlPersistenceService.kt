package ac.uk.ebi.biostd.persistence.integration.services

import ac.uk.ebi.biostd.persistence.common.service.PersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.exception.SequenceNotFoundException
import ac.uk.ebi.biostd.persistence.model.DbAccessTag
import ac.uk.ebi.biostd.persistence.model.Sequence
import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepo
import ac.uk.ebi.biostd.persistence.repositories.LockExecutor
import ac.uk.ebi.biostd.persistence.repositories.SequenceDataRepository
import org.springframework.transaction.annotation.Transactional

internal open class SqlPersistenceService(
    private val sequenceRepository: SequenceDataRepository,
    private val accessTagsDataRepository: AccessTagDataRepo,
    private val lockExecutor: LockExecutor,
    private val submissionQueryService: SubmissionQueryService
) : PersistenceService {
    override fun sequenceAccNoPatternExists(pattern: String): Boolean = sequenceRepository.existsByPrefix(pattern)

    override fun createAccNoPatternSequence(pattern: String) {
        sequenceRepository.save(Sequence(pattern))
    }

    @Transactional
    override fun getSequenceNextValue(pattern: String): Long = lockExecutor.executeLocking(pattern) {
        val sequence = sequenceRepository.findByPrefix(pattern) ?: throw SequenceNotFoundException(pattern)
        var next = sequence.counter.count + 1
        while (submissionQueryService.existByAccNo("${sequence.prefix}$next")) next++

        sequence.counter.count = next
        sequenceRepository.save(sequence).counter.count
    }

    override fun saveAccessTag(accessTag: String) {
        accessTagsDataRepository.save(DbAccessTag(name = accessTag))
    }

    override fun accessTagExists(accessTag: String): Boolean {
        return accessTagsDataRepository.existsByName(accessTag)
    }
}
