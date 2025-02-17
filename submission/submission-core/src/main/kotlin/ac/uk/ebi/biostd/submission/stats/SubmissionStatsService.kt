package ac.uk.ebi.biostd.submission.stats

import ac.uk.ebi.biostd.persistence.common.model.SubmissionStat
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType.DIRECTORIES
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType.FILES_SIZE
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType.NON_DECLARED_FILES_DIRECTORIES
import ac.uk.ebi.biostd.persistence.common.request.PageRequest
import ac.uk.ebi.biostd.persistence.common.service.StatsDataService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.doc.model.SingleSubmissionStat
import ebi.ac.uk.extended.model.ExtFileType
import ebi.ac.uk.extended.model.ExtSubmission
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import mu.KotlinLogging
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.filesFlow
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

private val logger = KotlinLogging.logger {}

class SubmissionStatsService(
    private val statsFileHandler: StatsFileHandler,
    private val submissionStatsService: StatsDataService,
    private val serializationService: ExtSerializationService,
    private val extSubmissionQueryService: SubmissionPersistenceQueryService,
) {
    suspend fun findByAccNo(accNo: String): List<SubmissionStat> = submissionStatsService.findByAccNo(accNo)

    fun findByType(
        type: String,
        filter: PageRequest,
    ): Flow<SubmissionStat> = submissionStatsService.findByType(SubmissionStatType.fromString(type.uppercase()), filter)

    suspend fun findByAccNoAndType(
        accNo: String,
        type: String,
    ): SubmissionStat = submissionStatsService.findByAccNoAndType(accNo, SubmissionStatType.fromString(type.uppercase()))

    suspend fun register(stat: SubmissionStat): SubmissionStat = submissionStatsService.save(stat)

    suspend fun register(
        type: String,
        stats: File,
    ) {
        val statsFlow = statsFileHandler.readStats(stats, SubmissionStatType.fromString(type.uppercase()))
        submissionStatsService.saveLast(statsFlow)
    }

    suspend fun increment(
        type: String,
        statsFile: File,
    ) {
        val statsFlow = statsFileHandler.readStats(statsFile, SubmissionStatType.fromString(type.uppercase()))
        submissionStatsService.incrementAll(statsFlow)
    }

    suspend fun calculateStats(accNo: String): List<SubmissionStat> {
        val sub = extSubmissionQueryService.getExtByAccNo(accNo, includeFileListFiles = true)
        logger.info { "${sub.accNo} ${sub.owner} Started calculating submission stats" }

        val stats = calculateStats(sub)
        val allStats = submissionStatsService.saveSubmissionStats(accNo, stats)
        logger.info { "${sub.accNo} ${sub.owner} Finished calculating submission stats. Files size: $stats" }
        return allStats
    }

    suspend fun refreshAll() {
        val idx = AtomicInteger(0)
        extSubmissionQueryService.findAllActive(includeFileListFiles = true).collect { sub ->
            val stats = calculateStats(sub)
            submissionStatsService.saveLast(stats.asFlow())
            logger.info { "Calculated stats submission ${sub.accNo}, ${idx.incrementAndGet()}" }
        }
    }

    private suspend fun calculateStats(sub: ExtSubmission): List<SingleSubmissionStat> {
        var subFilesSize = 0L
        var directories = mutableListOf<String>()

        serializationService
            .filesFlow(sub)
            .collect {
                if (it.type == ExtFileType.FILE) subFilesSize += it.size
                if (it.type == ExtFileType.DIR) directories.add(it.filePath.removeSuffix(".zip"))
            }

        val emptyDirectories = directories.count { hasFiles(it, sub) }

        return listOf(
            SingleSubmissionStat(sub.accNo, subFilesSize, FILES_SIZE),
            SingleSubmissionStat(sub.accNo, directories.size.toLong(), DIRECTORIES),
            SingleSubmissionStat(sub.accNo, emptyDirectories.toLong(), NON_DECLARED_FILES_DIRECTORIES),
        )
    }

    private suspend fun hasFiles(
        directoryPath: String,
        sub: ExtSubmission,
    ): Boolean =
        serializationService
            .filesFlow(sub)
            .filter { it.type == ExtFileType.FILE }
            .firstOrNull { it.filePath.contains(directoryPath) } != null
}
