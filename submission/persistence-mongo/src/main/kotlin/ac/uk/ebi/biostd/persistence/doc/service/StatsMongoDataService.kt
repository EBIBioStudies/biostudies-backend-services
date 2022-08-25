package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.exception.StatNotFoundException
import ac.uk.ebi.biostd.persistence.common.exception.StatsNotFoundException
import ac.uk.ebi.biostd.persistence.common.exception.SubmissionNotFoundException
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStat
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType
import ac.uk.ebi.biostd.persistence.common.request.PaginationFilter
import ac.uk.ebi.biostd.persistence.common.service.StatsDataService
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionStatsDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.repositories.SubmissionMongoRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionStats
import ac.uk.ebi.biostd.persistence.doc.model.SingleSubmissionStat
import org.springframework.data.domain.PageRequest

class StatsMongoDataService(
    private val submissionsRepository: SubmissionMongoRepository,
    private val statsDataRepository: SubmissionStatsDataRepository,
) : StatsDataService {
    override fun findByAccNo(accNo: String): List<SubmissionStat> =
        statsDataRepository
            .findByAccNo(accNo)
            ?.stats
            ?.map { SingleSubmissionStat(accNo, it.value, SubmissionStatType.fromString(it.key)) }
            ?: throw StatsNotFoundException(accNo)

    override fun findByType(submissionStatType: SubmissionStatType, filter: PaginationFilter): List<SubmissionStat> =
        statsDataRepository
            .findAllByStatType(submissionStatType, PageRequest.of(filter.pageNumber, filter.limit))
            .content
            .map { toSubmissionStat(submissionStatType, it) }

    override fun findByAccNoAndType(accNo: String, submissionStatType: SubmissionStatType): SubmissionStat =
        statsDataRepository
            .findByAccNoAndStatType(accNo, submissionStatType)
            ?.let { toSubmissionStat(submissionStatType, it) }
            ?: throw StatNotFoundException(accNo, submissionStatType)

    override fun save(stat: SubmissionStat): SubmissionStat {
        require(submissionsRepository.existsByAccNo(stat.accNo)) { throw SubmissionNotFoundException(stat.accNo) }

        return updateOrRegister(stat)
    }

    override fun saveAll(stats: List<SubmissionStat>): List<SubmissionStat> =
        stats.filterValid()
            .mapValues { it.value.last() }
            .map { updateOrRegister(it.value) }


    override fun incrementAll(stats: List<SubmissionStat>): List<SubmissionStat> =
        stats.filterValid()
            .mapValues { increment(it.key, it.value) }
            .values
            .toList()

    private fun List<SubmissionStat>.filterValid() =
        groupBy { it.accNo.uppercase() }
            .filter { submissionsRepository.existsByAccNo(it.key) }

    private fun updateOrRegister(update: SubmissionStat): SubmissionStat {
        statsDataRepository.updateOrRegisterStat(update)

        val updated = statsDataRepository.getByAccNo(update.accNo)

        return toSubmissionStat(update.type, updated)
    }

    private fun increment(accNo: String, increments: List<SubmissionStat>): SubmissionStat {
        statsDataRepository.incrementStat(accNo, increments)

        val type = increments.first().type
        val incremented = statsDataRepository.getByAccNo(accNo)

        return toSubmissionStat(type, incremented)
    }

    private fun toSubmissionStat(
        type: SubmissionStatType,
        docSubmission: DocSubmissionStats,
    ) = SingleSubmissionStat(docSubmission.accNo, docSubmission.stats[type.name]!!, type)
}
