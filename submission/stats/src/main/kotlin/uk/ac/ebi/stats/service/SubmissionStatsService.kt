package uk.ac.ebi.stats.service

import uk.ac.ebi.stats.exception.StatNotFoundException
import uk.ac.ebi.stats.mapping.SubmissionStatMapper
import uk.ac.ebi.stats.model.SubmissionStat
import uk.ac.ebi.stats.model.SubmissionStatType
import uk.ac.ebi.stats.persistence.model.SubmissionStatDb
import uk.ac.ebi.stats.persistence.repositories.SubmissionStatsRepository

class SubmissionStatsService(private val submissionStatsRepository: SubmissionStatsRepository) {
    fun findByType(submissionStatType: SubmissionStatType): List<SubmissionStat> =
        submissionStatsRepository.findAllByType(submissionStatType).map { SubmissionStatMapper.toSubmissionStat(it) }

    fun findByAccNoAndType(accNo: String, submissionStatType: SubmissionStatType): SubmissionStat =
        SubmissionStatMapper.toSubmissionStat(
            submissionStatsRepository.findByAccNoAndType(accNo, submissionStatType) ?:
            throw StatNotFoundException(accNo, submissionStatType))

    fun save(stat: SubmissionStat): SubmissionStat = when {
        exists(stat.accNo, stat.type).not() -> insert(stat)
        else -> update(stat)
    }

    fun update(stat: SubmissionStat): SubmissionStat {
        val oldStat =
            submissionStatsRepository.findByAccNoAndType(stat.accNo, stat.type) ?:
            throw StatNotFoundException(stat.accNo, stat.type)
        val newStat = SubmissionStatDb(oldStat.accNo, stat.value, oldStat.type).apply { id = oldStat.id }

        return SubmissionStatMapper.toSubmissionStat(submissionStatsRepository.save(newStat))
    }

    private fun insert(stat: SubmissionStat): SubmissionStat {
        val saved = SubmissionStatMapper.toSubmissionStatDb(stat)
        return SubmissionStatMapper.toSubmissionStat(submissionStatsRepository.save(saved))
    }

    private fun exists(accNo: String, type: SubmissionStatType) =
        submissionStatsRepository.existsByAccNoAndType(accNo, type)
}
