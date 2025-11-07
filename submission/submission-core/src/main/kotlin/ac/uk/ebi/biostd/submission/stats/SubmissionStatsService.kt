package ac.uk.ebi.biostd.submission.stats

import ac.uk.ebi.biostd.persistence.common.model.SubmissionStat
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType
import ac.uk.ebi.biostd.persistence.common.request.PageRequest
import ac.uk.ebi.biostd.persistence.common.service.StatsDataService
import ebi.ac.uk.model.UpdateResult
import kotlinx.coroutines.flow.Flow
import java.io.File

class SubmissionStatsService(
    private val statsFileHandler: StatsFileHandler,
    private val statsDataService: StatsDataService,
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

    suspend fun deleteByAccNo(accNo: String) = statsDataService.deleteStatsByAccNo(accNo)
}
