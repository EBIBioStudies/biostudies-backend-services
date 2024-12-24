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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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

    override suspend fun saveLast(stats: List<SubmissionStat>): List<SubmissionStat> =
        stats
            .filterValid()
            .mapValues { it.value.last() }
            .map { updateOrRegister(it.value) }

    override suspend fun incrementAll(stats: List<SubmissionStat>): List<SubmissionStat> =
        stats
            .filterValid()
            .mapValues { increment(it.key, it.value) }
            .values
            .toList()

    private suspend fun List<SubmissionStat>.filterValid() =
        groupBy { it.accNo.uppercase() }
            .filter { submissionsRepository.existsByAccNo(it.key) }

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
