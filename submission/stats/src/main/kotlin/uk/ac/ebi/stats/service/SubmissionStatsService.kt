package uk.ac.ebi.stats.service

import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType
import ac.uk.ebi.biostd.persistence.common.request.PaginationFilter
import ac.uk.ebi.biostd.persistence.common.service.SubmissionMetaQueryService
import ac.uk.ebi.biostd.persistence.exception.SubmissionNotFoundException
import ac.uk.ebi.biostd.persistence.model.DbSubmissionStat
import ac.uk.ebi.biostd.persistence.repositories.SubmissionStatsDataRepository
import org.springframework.data.domain.PageRequest
import org.springframework.transaction.annotation.Transactional
import uk.ac.ebi.stats.exception.StatNotFoundException
import uk.ac.ebi.stats.mapping.SubmissionStatMapper.toSubmissionStat
import uk.ac.ebi.stats.mapping.SubmissionStatMapper.toSubmissionStatDb
import uk.ac.ebi.stats.model.SubmissionStat

@SuppressWarnings("TooManyFunctions")
open class SubmissionStatsService(
    private val submissionQueryService: SubmissionMetaQueryService,
    private val statsRepository: SubmissionStatsDataRepository
) {
    open fun findByType(submissionStatType: SubmissionStatType, filter: PaginationFilter): List<SubmissionStat> =
        statsRepository
            .findAllByType(submissionStatType, PageRequest.of(filter.pageNumber, filter.limit))
            .content
            .map { toSubmissionStat(it) }

    open fun findByAccNoAndType(accNo: String, submissionStatType: SubmissionStatType): SubmissionStat =
        toSubmissionStat(
            statsRepository.findByAccNoAndType(accNo, submissionStatType)
            ?: throw StatNotFoundException(accNo, submissionStatType))

    open fun save(stat: SubmissionStat): SubmissionStat {
        require(submissionQueryService.existByAccNo(stat.accNo)) { throw SubmissionNotFoundException(stat.accNo) }
        return toSubmissionStat(statsRepository.save(toUpsert(stat)))
    }

    @Transactional
    open fun saveAll(stats: List<SubmissionStat>): List<SubmissionStat> {
        val updates = summarize(normalize(stats)) { it.last() }
        return statsRepository
            .saveAll(updates.map(::toUpsert))
            .map(::toSubmissionStat)
    }

    @Transactional
    open fun incrementAll(stats: List<SubmissionStat>): List<SubmissionStat> {
        val increments = summarize(normalize(stats), ::summarizeIncrements)
        return statsRepository
            .saveAll(increments.map(::toIncrement))
            .map(::toSubmissionStat)
    }

    private fun normalize(stats: List<SubmissionStat>) =
        stats.filter { submissionQueryService.existByAccNo(it.accNo) }
            .map { SubmissionStat(it.accNo.toUpperCase(), it.value, it.type) }

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
        return SubmissionStat(reference.accNo, summarized, reference.type)
    }

    private fun toUpsert(stat: SubmissionStat) = toUpdate(stat) { _, newValue -> newValue }

    private fun toIncrement(stat: SubmissionStat) = toUpdate(stat) { currentValue, newValue -> currentValue + newValue }

    private fun toUpdate(
        stat: SubmissionStat,
        updatedValue: (currentValue: Long, newValue: Long) -> Long
    ): DbSubmissionStat = when (val statDb = statsRepository.findByAccNoAndType(stat.accNo, stat.type)) {
        null -> toSubmissionStatDb(stat)
        else -> DbSubmissionStat(
            statDb.accNo, updatedValue(statDb.value, stat.value), statDb.type).apply { id = statDb.id }
    }
}
