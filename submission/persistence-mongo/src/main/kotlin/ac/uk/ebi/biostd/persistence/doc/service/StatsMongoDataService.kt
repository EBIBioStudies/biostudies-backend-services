package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.exception.StatNotFoundException
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStat
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType
import ac.uk.ebi.biostd.persistence.common.request.PaginationFilter
import ac.uk.ebi.biostd.persistence.common.service.StatsDataService
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionStatsDataRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.SingleSubmissionStat
import org.springframework.data.domain.PageRequest

class StatsMongoDataService(
    private val statsDataRepository: SubmissionStatsDataRepository
) : StatsDataService {
    override fun findByType(submissionStatType: SubmissionStatType, filter: PaginationFilter): List<SubmissionStat> =
        statsDataRepository
            .findAllByStatType(submissionStatType, PageRequest.of(filter.pageNumber, filter.limit))
            .content
            .map { toSubmissionStat(it, submissionStatType) }

    override fun findByAccNoAndType(accNo: String, submissionStatType: SubmissionStatType): SubmissionStat =
        statsDataRepository
            .findByAccNoAndStatType(accNo, submissionStatType)
            ?.let { toSubmissionStat(it, submissionStatType) }
            ?: throw StatNotFoundException(accNo, submissionStatType)

    // TODO support saving stats for expired versions Pivotal ID # 176802128
    override fun saveAll(stats: List<SubmissionStat>): List<SubmissionStat> =
        stats.filterValid()
            .mapValues { it.value.last() }
            .map { updateSubmissionStat(it.key!!, it.value) }

    override fun incrementAll(stats: List<SubmissionStat>): List<SubmissionStat> =
        stats.filterValid()
            .mapValues { incrementSubmissionStat(it.key!!, it.value) }
            .values
            .toList()

    private fun List<SubmissionStat>.filterValid() =
        groupBy { it.accNo.uppercase() }
            .mapKeys { statsDataRepository.findByAccNo(it.key.uppercase()) }
            .filterKeys { it != null }

    private fun updateSubmissionStat(sub: DocSubmission, update: SubmissionStat): SubmissionStat {
        statsDataRepository.updateStat(sub.accNo, sub.version, update)
        return update
    }

    private fun incrementSubmissionStat(sub: DocSubmission, increments: List<SubmissionStat>): SubmissionStat {
        val type = increments.first().type
        val current = sub.stats.find { it.name == type.name }?.value ?: 0L
        val increment = statsDataRepository.incrementStat(sub.accNo, sub.version, increments)

        return SingleSubmissionStat(sub.accNo, current + increment, type)
    }

    private fun toSubmissionStat(docSubmission: DocSubmission, type: SubmissionStatType): SubmissionStat {
        val stat = docSubmission.stats.first { it.name == type.name }
        return SingleSubmissionStat(docSubmission.accNo, stat.value, type)
    }
}
