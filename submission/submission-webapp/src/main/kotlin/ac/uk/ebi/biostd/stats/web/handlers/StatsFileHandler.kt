package ac.uk.ebi.biostd.stats.web.handlers

import ebi.ac.uk.util.collections.second
import uk.ac.ebi.stats.model.SubmissionStat
import uk.ac.ebi.stats.model.SubmissionStatType
import java.io.File

class StatsFileHandler {
    fun readBulkStats(stats: File, type: SubmissionStatType): List<SubmissionStat> =
        stats.readLines()
            .map { it.split("\t") }
            .map { SubmissionStat(accNo = it.first(), value = it.second().toLong(), type = type) }
}
