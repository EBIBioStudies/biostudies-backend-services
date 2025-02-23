package ac.uk.ebi.biostd.submission.stats

import ac.uk.ebi.biostd.persistence.common.model.SubmissionStat
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType
import ebi.ac.uk.util.collections.second
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import java.io.File

private val logger = KotlinLogging.logger {}

class StatsFileHandler {
    /**
     * Read the list of stats in the file. Invalid entries are removed. Note that is likely that file contains
     * duplicated entries for the same accNo. Which represent consecutive stats updates.
     */
    suspend fun readStatsForIncrement(
        stats: File,
        type: SubmissionStatType,
    ): List<SubmissionStat> =
        withContext(Dispatchers.IO) {
            stats
                .readLines()
                .map { asStat(it, type) }
                .filterNotNull()
        }

    /**
     * Read the list of stats in the file. Invalid entries are removed. Note that as stats values are for register
     * latest value reported is the only keep by accNo.
     */
    suspend fun readRegisterStats(
        stats: File,
        type: SubmissionStatType,
    ): List<SubmissionStat> =
        withContext(Dispatchers.IO) {
            stats
                .readLines()
                .map { asStat(it, type) }
                .filterNotNull()
                .associateBy({ it.accNo }, { it })
                .map { it.value }
        }

    @Suppress("ReturnCount")
    private suspend fun asStat(
        entry: String,
        type: SubmissionStatType,
    ): SubmissionStat? {
        val record = entry.split("\t")
        if (record.size != 2) {
            logger.info { "Ignoring malformed entry '$entry'" }
            return null
        }

        val value = record[1].toLongOrNull()
        if (value == null) {
            logger.info { "Ignoring entry as incorrect value '$entry'" }
            return null
        }

        return SubmissionStat(record.first(), record.second().toLong(), type = type)
    }
}
