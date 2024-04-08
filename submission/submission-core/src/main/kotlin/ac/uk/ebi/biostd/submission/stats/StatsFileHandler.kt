package ac.uk.ebi.biostd.submission.stats

import ac.uk.ebi.biostd.persistence.common.model.SubmissionStat
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType
import ac.uk.ebi.biostd.persistence.doc.model.SingleSubmissionStat
import ebi.ac.uk.util.collections.second
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class StatsFileHandler {
    suspend fun readStats(
        stats: File,
        type: SubmissionStatType,
    ): List<SubmissionStat> =
        withContext(Dispatchers.IO) {
            stats.readLines()
                .map { it.split("\t") }
                .map { readStat(it, type) }
        }

    private fun readStat(
        stat: List<String>,
        type: SubmissionStatType,
    ): SubmissionStat {
        require(stat.size == 2) { throw InvalidStatException("The stats should have accNo and value") }
        return SingleSubmissionStat(accNo = stat.first(), value = stat.second().toLong(), type = type)
    }
}
