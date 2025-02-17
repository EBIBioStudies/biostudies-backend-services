package ac.uk.ebi.biostd.submission.stats

import ac.uk.ebi.biostd.persistence.common.model.SubmissionStat
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType
import ac.uk.ebi.biostd.persistence.doc.model.SingleSubmissionStat
import ebi.ac.uk.io.ext.asFlow
import ebi.ac.uk.util.collections.second
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File

class StatsFileHandler {
    suspend fun readStats(
        stats: File,
        type: SubmissionStatType,
    ): Flow<SubmissionStat> =
        withContext(Dispatchers.IO) {
            val reader = stats.inputStream().bufferedReader()
            reader
                .asFlow()
                .map { it.split("\t") }
                .filter { it.size == 2 }
                .map { SingleSubmissionStat(accNo = it.first(), value = it.second().toLong(), type = type) }
        }
}
