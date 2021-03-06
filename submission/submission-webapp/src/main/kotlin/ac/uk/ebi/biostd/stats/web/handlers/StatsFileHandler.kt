package ac.uk.ebi.biostd.stats.web.handlers

import ac.uk.ebi.biostd.persistence.common.model.SubmissionStat
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType
import ac.uk.ebi.biostd.persistence.model.DbSubmissionStat
import ac.uk.ebi.biostd.stats.web.exceptions.InvalidStatException
import ebi.ac.uk.util.collections.second
import java.io.File

class StatsFileHandler {
    fun readStats(stats: File, type: SubmissionStatType): List<SubmissionStat> =
        stats.readLines()
            .map { it.split("\t") }
            .map { readStat(it, type) }

    private fun readStat(stat: List<String>, type: SubmissionStatType): SubmissionStat {
        require(stat.size == 2) { throw InvalidStatException("The stats should have accNo and value") }
        return DbSubmissionStat(accNo = stat.first(), value = stat.second().toLong(), type = type)
    }
}
