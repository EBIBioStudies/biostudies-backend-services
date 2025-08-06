package ac.uk.ebi.biostd.submission.stats

import ac.uk.ebi.biostd.persistence.common.model.SubmissionStat
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType
import ac.uk.ebi.biostd.persistence.common.request.PageRequest
import ac.uk.ebi.biostd.persistence.common.service.StatsDataService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ebi.ac.uk.model.UpdateResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import mu.KotlinLogging
import java.io.File
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.atomic.AtomicInteger

private val logger = KotlinLogging.logger {}

class SubmissionStatsService(
    private val statsFileHandler: StatsFileHandler,
    private val statsDataService: StatsDataService,
    private val extSubQueryService: SubmissionPersistenceQueryService,
    private val submissionStatsCalculator: SubmissionStatsCalculator,
) {
    suspend fun findByAccNo(accNo: String): List<SubmissionStat> = statsDataService.findStatsByAccNo(accNo)

    fun findByType(
        type: String,
        filter: PageRequest,
    ): Flow<SubmissionStat> = statsDataService.findStatsByType(SubmissionStatType.fromString(type.uppercase()), filter)

    suspend fun findByAccNoAndType(
        accNo: String,
        type: String,
    ): SubmissionStat = statsDataService.findStatByAccNoAndType(accNo, SubmissionStatType.fromString(type.uppercase()))

    suspend fun register(stat: SubmissionStat): SubmissionStat = statsDataService.saveStat(stat)

    suspend fun register(
        type: String,
        statsFile: File,
    ): UpdateResult {
        val stats = statsFileHandler.readRegisterStats(statsFile, SubmissionStatType.fromString(type.uppercase()))
        val result = statsDataService.saveAll(stats)
        return UpdateResult(
            insertedRecords = result.insertedCount + result.upserts.size,
            modifiedRecords = result.modifiedCount,
        )
    }

    suspend fun increment(
        type: String,
        statsFile: File,
    ): UpdateResult {
        val stats = statsFileHandler.readStatsForIncrement(statsFile, SubmissionStatType.fromString(type.uppercase()))
        val result = statsDataService.incrementAll(stats)
        return UpdateResult(
            insertedRecords = result.insertedCount + result.upserts.size,
            modifiedRecords = result.modifiedCount,
        )
    }

    suspend fun calculateStats(accNo: String): List<SubmissionStat> {
        refreshStats(accNo)
        val stats = statsDataService.findByAccNo(accNo)?.stats.orEmpty()
        logger.info { "Finished calculating submission '$accNo' stats. $stats" }
        return stats
    }

    suspend fun refreshAll() {
        val idx = AtomicInteger(0)
        statsDataService
            .findAll(Instant.now().minus(REFRESH_DAYS, ChronoUnit.DAYS))
            .onEach { accNo -> "Calculating stats ${idx.incrementAndGet()} accNo '$accNo'" }
            .collect({ accNo -> refreshStatsSafely(accNo) })
    }

    private suspend fun refreshStatsSafely(accNo: String) {
        runCatching { refreshStats(accNo) }
            .onFailure { logger.error(it) { "Issues calculating stats for accNo: '$accNo'" } }
            .onSuccess { logger.info { "Finished calculating stats for accNo: '$accNo'" } }
    }

    private suspend fun refreshStats(accNo: String) {
        val sub = extSubQueryService.getExtByAccNo(accNo, includeFileListFiles = true, includeLinkListLinks = true)
        logger.info { "${sub.accNo} ${sub.owner} Started calculating submission stats" }

        val stats = submissionStatsCalculator.calculateStats(sub)
        statsDataService.saveAll(accNo, stats)
    }

    companion object {
        const val REFRESH_DAYS = 30L
    }
}
