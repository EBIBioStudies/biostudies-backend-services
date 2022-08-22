package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.exception.StatNotFoundException
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStat
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType
import ac.uk.ebi.biostd.persistence.common.request.PaginationFilter
import ac.uk.ebi.biostd.persistence.common.service.StatsDataService
import ac.uk.ebi.biostd.persistence.doc.db.repositories.SubmissionMongoRepository
import ac.uk.ebi.biostd.persistence.doc.db.repositories.SubmissionStatsRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocStat
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionStats
import ac.uk.ebi.biostd.persistence.doc.model.SingleSubmissionStat
import org.bson.types.ObjectId
import org.springframework.data.domain.PageRequest

class StatsMongoDataService(
    private val statsRepository: SubmissionStatsRepository,
    private val submissionsRepository: SubmissionMongoRepository,
) : StatsDataService {
    override fun findByType(submissionStatType: SubmissionStatType, filter: PaginationFilter): List<SubmissionStat> =
        statsRepository
            .findAllByStatType(submissionStatType, PageRequest.of(filter.pageNumber, filter.limit))
            .content
            .map { toSubmissionStat(it, submissionStatType) }

    override fun findByAccNoAndType(accNo: String, submissionStatType: SubmissionStatType): SubmissionStat =
        statsRepository
            .findByAccNoAndStatType(accNo, submissionStatType)
            ?.let { toSubmissionStat(it, submissionStatType) }
            ?: throw StatNotFoundException(accNo, submissionStatType)

    override fun save(stat: SubmissionStat): SubmissionStat {
        updateOrRegister(stat)
        return stat
    }

    override fun saveAll(stats: List<SubmissionStat>): List<SubmissionStat> {
        val valid = stats.filterValid().map { it.value.last() }
        valid.forEach { updateOrRegister(it) }

        return valid
    }

    override fun incrementAll(stats: List<SubmissionStat>): List<SubmissionStat> =
        stats.filterValid()
            .mapValues { incrementStat(it.value) }
            .values
            .toList()

    private fun List<SubmissionStat>.filterValid() =
        groupBy { it.accNo.uppercase() }
            .filter { submissionsRepository.existsByAccNo(it.key) }

    private fun updateOrRegister(stat: SubmissionStat) {
        val docStat = DocStat(stat.type.name, stat.value)
        val current = statsRepository.findByAccNo(stat.accNo)

        if (current != null) {
            val stats = current.stats.filterNot { it.name == stat.type.name }.plus(docStat)
            statsRepository.save(current.copy(stats = stats))
        } else {
            statsRepository.save(DocSubmissionStats(ObjectId(), stat.accNo, listOf(docStat)))
        }
    }

    private fun incrementStat(increments: List<SubmissionStat>): SubmissionStat {
        val type = increments.first().type
        val accNo = increments.first().accNo
        val current = statsRepository.findByAccNo(accNo)?.stats?.find { it.name == type.name }?.value ?: 0L
        val increment = increments.fold(current) { acc, submissionStat -> acc + submissionStat.value }
        val incremented = SingleSubmissionStat(accNo, increment, type)

        updateOrRegister(incremented)

        return incremented
    }

    private fun toSubmissionStat(docSubmission: DocSubmissionStats, type: SubmissionStatType): SubmissionStat {
        val stat = docSubmission.stats.first { it.name == type.name }
        return SingleSubmissionStat(docSubmission.accNo, stat.value, type)
    }
}
