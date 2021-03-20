package ac.uk.ebi.biostd.persistence.service

import ac.uk.ebi.biostd.persistence.common.exception.StatNotFoundException
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStat
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType
import ac.uk.ebi.biostd.persistence.common.request.PaginationFilter
import ac.uk.ebi.biostd.persistence.common.service.StatsDataService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionMetaQueryService
import ac.uk.ebi.biostd.persistence.model.DbSubmissionStat
import ac.uk.ebi.biostd.persistence.repositories.SubmissionStatsDataRepository
import org.springframework.data.domain.PageRequest
import org.springframework.transaction.annotation.Transactional

open class StatsSqlDataService(
    private val submissionQueryService: SubmissionMetaQueryService,
    private val statsRepository: SubmissionStatsDataRepository
) : StatsDataService {
    override fun findByType(submissionStatType: SubmissionStatType, filter: PaginationFilter): List<SubmissionStat> =
        statsRepository
            .findAllByType(submissionStatType, PageRequest.of(filter.pageNumber, filter.limit))
            .content

    override fun findByAccNoAndType(accNo: String, submissionStatType: SubmissionStatType): SubmissionStat =
        statsRepository
            .findByAccNoAndType(accNo, submissionStatType)
            ?: throw StatNotFoundException(accNo, submissionStatType)

    @Transactional
    override fun saveAll(stats: List<SubmissionStat>): List<SubmissionStat> {
        val updates = summarize(normalize(stats)) { it.last() }
        return statsRepository
            .saveAll(updates.map(::toUpsert))
            .toList()
    }

    @Transactional
    override fun incrementAll(stats: List<SubmissionStat>): List<SubmissionStat> {
        val increments = summarize(normalize(stats), ::summarizeIncrements)
        return statsRepository
            .saveAll(increments.map(::toIncrement))
            .toList()
    }

    private fun normalize(stats: List<SubmissionStat>) =
        stats.filter { submissionQueryService.existByAccNo(it.accNo) }
            .map { DbSubmissionStat(it.accNo.toUpperCase(), it.value, it.type) }

    private fun summarize(
        stats: List<SubmissionStat>,
        summarizeMethod: (stats: List<SubmissionStat>) -> SubmissionStat
    ): List<SubmissionStat> =
        stats.groupBy { it.accNo }
            .mapValues { summarizeMethod(it.value) }
            .entries
            .map { it.value }

    private fun summarizeIncrements(stats: List<SubmissionStat>): SubmissionStat {
        val reference = stats.first()
        val summarized = stats.map { it.value }.reduce { acc, stat -> acc + stat }
        return DbSubmissionStat(reference.accNo, summarized, reference.type)
    }

    private fun toUpsert(stat: SubmissionStat) = toUpdate(stat) { _, newValue -> newValue }

    private fun toIncrement(stat: SubmissionStat) = toUpdate(stat) { currentValue, newValue -> currentValue + newValue }

    private fun toUpdate(
        stat: SubmissionStat,
        updatedValue: (currentValue: Long, newValue: Long) -> Long
    ): DbSubmissionStat = when (val statDb = statsRepository.findByAccNoAndType(stat.accNo, stat.type)) {
        null -> DbSubmissionStat(stat.accNo, stat.value, stat.type)
        else -> DbSubmissionStat(
            statDb.accNo, updatedValue(statDb.value, stat.value), statDb.type).apply { id = statDb.id }
    }
}
