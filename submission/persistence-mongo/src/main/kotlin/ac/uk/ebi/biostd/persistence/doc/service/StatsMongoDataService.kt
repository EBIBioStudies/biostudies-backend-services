package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.exception.StatNotFoundException
import ac.uk.ebi.biostd.persistence.common.exception.StatsNotFoundException
import ac.uk.ebi.biostd.persistence.common.exception.SubmissionNotFoundException
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStat
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType
import ac.uk.ebi.biostd.persistence.common.request.PageRequest
import ac.uk.ebi.biostd.persistence.common.service.StatsDataService
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionStatsDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.reactive.repositories.SubmissionMongoRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionStats
import ac.uk.ebi.biostd.persistence.doc.model.SingleSubmissionStat
import hu.akarnokd.kotlin.flow.groupBy
import hu.akarnokd.kotlin.flow.toList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import org.springframework.data.domain.PageRequest as DataPageRequest

@Suppress("TooManyFunctions")
class StatsMongoDataService(
    private val submissionsRepository: SubmissionMongoRepository,
    private val statsDataRepository: SubmissionStatsDataRepository,
) : StatsDataService {
    override suspend fun findByAccNo(accNo: String): List<SubmissionStat> =
        statsDataRepository
            .findByAccNo(accNo)
            ?.stats
            ?.map { SingleSubmissionStat(accNo, it.value, SubmissionStatType.fromString(it.key)) }
            ?: throw StatsNotFoundException(accNo)

    override fun findByType(
        submissionStatType: SubmissionStatType,
        filter: PageRequest,
    ): Flow<SubmissionStat> =
        statsDataRepository
            .findAllByStatType(submissionStatType, DataPageRequest.of(filter.pageNumber, filter.limit))
            .map { toSubmissionStat(submissionStatType, it) }

    override suspend fun findByAccNoAndType(
        accNo: String,
        submissionStatType: SubmissionStatType,
    ): SubmissionStat =
        statsDataRepository
            .findByAccNoAndStatType(accNo, submissionStatType)
            ?.let { toSubmissionStat(submissionStatType, it) }
            ?: throw StatNotFoundException(accNo, submissionStatType)

    override suspend fun save(stat: SubmissionStat): SubmissionStat {
        require(submissionsRepository.existsByAccNo(stat.accNo)) { throw SubmissionNotFoundException(stat.accNo) }

        return updateOrRegister(stat)
    }

    override suspend fun saveSubmissionStats(
        accNo: String,
        stats: List<SubmissionStat>,
    ): List<SubmissionStat> {
        val submissionStats = stats.associateBy({ it.type.name }, { it.value })
        return statsDataRepository
            .upsertStats(accNo, submissionStats)
            .stats
            .map { SingleSubmissionStat(accNo = accNo, type = SubmissionStatType.fromString(it.key), value = it.value) }
    }

    override suspend fun saveLast(stats: Flow<SubmissionStat>) {
        stats
            .groupBy { it.accNo.uppercase() }
            .mapLatest { it }
            .flatMapMerge { it }
            .filter { submissionsRepository.existsByAccNo(it.accNo) }
            .onEach { updateOrRegister(it) }
            .collect()
    }

    override suspend fun incrementAll(stats: Flow<SubmissionStat>) {
        stats
            .groupBy { it.accNo.uppercase() }
            .flatMapMerge { it.toList() }
            .filter { submissionsRepository.existsByAccNo(it.first().accNo) }
            .onEach { increment(it.first().accNo, it) }
            .collect()
    }

    private suspend fun updateOrRegister(update: SubmissionStat): SubmissionStat {
        statsDataRepository.updateOrRegisterStat(update)

        val updated = statsDataRepository.getByAccNo(update.accNo)

        return toSubmissionStat(update.type, updated)
    }

    private suspend fun increment(
        accNo: String,
        increments: List<SubmissionStat>,
    ): SubmissionStat {
        statsDataRepository.incrementStat(accNo, increments)

        val type = increments.first().type
        val incremented = statsDataRepository.getByAccNo(accNo)

        return toSubmissionStat(type, incremented)
    }

    private fun toSubmissionStat(
        type: SubmissionStatType,
        docSubmission: DocSubmissionStats,
    ): SingleSubmissionStat = SingleSubmissionStat(docSubmission.accNo, docSubmission.stats[type.name]!!, type)
}
