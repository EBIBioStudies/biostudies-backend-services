package ac.uk.ebi.biostd.persistence.integration.services

import ac.uk.ebi.biostd.persistence.common.service.PersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.exception.SequenceNotFoundException
import ac.uk.ebi.biostd.persistence.model.DbAccessTag
import ac.uk.ebi.biostd.persistence.model.DbSequence
import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepo
import ac.uk.ebi.biostd.persistence.repositories.LockExecutor
import ac.uk.ebi.biostd.persistence.repositories.SequenceDataRepository
import kotlinx.coroutines.runBlocking
import org.springframework.transaction.annotation.Transactional

internal open class SqlPersistenceService(
    private val sequenceRepository: SequenceDataRepository,
    private val accessTagsDataRepository: AccessTagDataRepo,
    private val lockExecutor: LockExecutor,
    private val submissionPersistenceQueryService: SubmissionPersistenceQueryService,
) : PersistenceService {
    override fun sequenceAccNoPatternExists(pattern: String): Boolean = sequenceRepository.existsByPrefix(pattern)

    override fun createAccNoPatternSequence(pattern: String) {
        sequenceRepository.save(DbSequence(pattern))
    }

    @Transactional
    override fun getSequenceNextValue(pattern: String): Long {
        fun getNextSequence(pattern: String): Long =
            runBlocking {
                val sequence = sequenceRepository.findByPrefix(pattern) ?: throw SequenceNotFoundException(pattern)
                var next = sequence.counter.count + 1
                while (submissionPersistenceQueryService.existByAccNo("${sequence.prefix}$next")) next++

                sequence.counter.count = next
                val saved = sequenceRepository.save(sequence)
                saved.counter.count
            }
        return lockExecutor.executeLocking(pattern) { getNextSequence(pattern) }
    }

    override fun saveAccessTag(accessTag: String) {
        accessTagsDataRepository.save(DbAccessTag(name = accessTag))
    }

    override fun accessTagExists(accessTag: String): Boolean {
        return accessTagsDataRepository.existsByName(accessTag)
    }
}
