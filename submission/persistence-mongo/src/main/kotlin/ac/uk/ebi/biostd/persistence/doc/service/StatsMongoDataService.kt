package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.exception.StatNotFoundException
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStat
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType
import ac.uk.ebi.biostd.persistence.common.request.PaginationFilter
import ac.uk.ebi.biostd.persistence.common.service.StatsDataService
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocStat
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.SingleSubmissionStat
import org.springframework.data.domain.PageRequest

class StatsMongoDataService(
    private val subDataRepository: SubmissionDocDataRepository
) : StatsDataService {
    override fun findByType(submissionStatType: SubmissionStatType, filter: PaginationFilter): List<SubmissionStat> =
        subDataRepository
            .findAllByStatType(submissionStatType, PageRequest.of(filter.pageNumber, filter.limit))
            .content
            .map { toSubmissionStat(it, submissionStatType) }

    override fun findByAccNoAndType(accNo: String, submissionStatType: SubmissionStatType): SubmissionStat =
        subDataRepository
            .findByAccNoAndStatType(accNo, submissionStatType)
            ?.let { toSubmissionStat(it, submissionStatType) }
            ?: throw StatNotFoundException(accNo, submissionStatType)

    override fun saveAll(stats: List<SubmissionStat>): List<SubmissionStat> =
        stats.filterValid()
            .mapValues { it.value.last() }
            .map { updateSubmissionStats(it.key!!, it.value) }

    override fun incrementAll(stats: List<SubmissionStat>): List<SubmissionStat> =
        stats.filterValid()
            .mapValues { summarizeIncrements(it.key!!.stats, it.value) }
            .map { updateSubmissionStats(it.key!!, it.value) }

    private fun List<SubmissionStat>.filterValid() =
        groupBy { it.accNo }
        .mapKeys { subDataRepository.findByAccNo(it.key.toUpperCase()) }
        .filterKeys { it != null }

    private fun updateSubmissionStats(sub: DocSubmission, update: SubmissionStat): SubmissionStat {
        subDataRepository.updateStats(sub.accNo, sub.version, updatedStats(update, sub.stats))
        return update
    }

    private fun updatedStats(update: SubmissionStat, currentStats: List<DocStat>): List<DocStat> =
        currentStats
            .filterNot { it.name == update.type.name }
            .plus(DocStat(update.type.name, update.value))

    private fun summarizeIncrements(currentStats: List<DocStat>, increments: List<SubmissionStat>): SubmissionStat {
        val reference = increments.first()
        val current = currentStats.find { it.name == reference.type.name } ?: DocStat(reference.type.name, 0L)
        val increment = increments.map { stat -> stat.value }.reduce { total, stat -> total + stat }

        return SingleSubmissionStat(reference.accNo, current.value + increment, reference.type)
    }

    private fun toSubmissionStat(docSubmission: DocSubmission, type: SubmissionStatType): SubmissionStat {
        val stat = docSubmission.stats.first { it.name == type.name }
        return SingleSubmissionStat(docSubmission.accNo, stat.value, type)
    }
}
