package ac.uk.ebi.biostd.submission.stats

import ac.uk.ebi.biostd.persistence.common.model.SubmissionStat
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType.DIRECTORIES
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType.FILES_SIZE
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType.NON_DECLARED_FILES_DIRECTORIES
import ac.uk.ebi.biostd.persistence.common.request.PageRequest
import ac.uk.ebi.biostd.persistence.common.service.StatsDataService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ebi.ac.uk.extended.model.ExtFileType
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.PersistedExtFile
import ebi.ac.uk.model.UpdateResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onEach
import mu.KotlinLogging
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.filesFlow
import java.io.File
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.atomic.AtomicInteger

private val logger = KotlinLogging.logger {}

class SubmissionStatsService(
    private val statsFileHandler: StatsFileHandler,
    private val statsDataService: StatsDataService,
    private val serializationService: ExtSerializationService,
    private val extSubQueryService: SubmissionPersistenceQueryService,
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
        val sub = extSubQueryService.getExtByAccNo(accNo, includeFileListFiles = true, includeLinkListLinks = true)
        logger.info { "${sub.accNo} ${sub.owner} Started calculating submission stats" }

        val stats = calculateStats(sub)
        statsDataService.saveAll(accNo, stats)
        logger.info { "${sub.accNo} ${sub.owner} Finished calculating submission stats. Files size: $stats" }
        return statsDataService.findByAccNo(accNo)?.stats.orEmpty()
    }

    suspend fun refreshAll() {
        val idx = AtomicInteger(0)
        extSubQueryService
            .findAllActive(includeFileListFiles = true, includeLinkListLinks = true)
            .filter {
                val lastUpdated = statsDataService.lastUpdated(it.accNo)
                lastUpdated == null || lastUpdated.isBefore(Instant.now().minus(REFRESH_DAYS, ChronoUnit.DAYS))
            }.onEach { sub ->
                val stats = calculateStats(sub)
                statsDataService.saveAll(sub.accNo, stats)
                logger.info { "Calculated stats submission ${idx.incrementAndGet()}. accNo='${sub.accNo}'" }
            }.collect()
    }

    private suspend fun calculateStats(sub: ExtSubmission): List<SubmissionStat> {
        logger.info { "Calculating stats for submission ${sub.accNo}, version ${sub.version}" }
        var subFilesSize = 0L
        var directories = mutableListOf<String>()

        serializationService
            .filesFlow(sub)
            .filterIsInstance<PersistedExtFile>()
            .collect {
                if (it.type == ExtFileType.FILE) subFilesSize += it.size
                if (it.type == ExtFileType.DIR) directories.add(it.filePath.removeSuffix(".zip"))
            }

        val emptyDirectories = directories.count { hasFiles(it, sub) }

        return listOf(
            SubmissionStat(sub.accNo, subFilesSize, FILES_SIZE),
            SubmissionStat(sub.accNo, directories.size.toLong(), DIRECTORIES),
            SubmissionStat(sub.accNo, emptyDirectories.toLong(), NON_DECLARED_FILES_DIRECTORIES),
        )
    }

    private suspend fun hasFiles(
        directoryPath: String,
        sub: ExtSubmission,
    ): Boolean =
        serializationService
            .filesFlow(sub)
            .filterIsInstance<PersistedExtFile>()
            .filter { it.type == ExtFileType.FILE }
            .firstOrNull { it.filePath.contains(directoryPath) } != null

    companion object {
        const val REFRESH_DAYS = 30L
    }
}
