package ac.uk.ebi.biostd.submission.stats

import ac.uk.ebi.biostd.persistence.common.model.SubmissionStat
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType.FILES_SIZE
import ac.uk.ebi.biostd.persistence.common.request.PageRequest
import ac.uk.ebi.biostd.persistence.common.service.StatsDataService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.doc.model.SingleSubmissionStat
import ebi.ac.uk.extended.model.ExtSubmission
import kotlinx.coroutines.flow.Flow
import mu.KotlinLogging
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.filesFlow
import java.io.File

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
    ): List<SubmissionStat> {
        val statsList = statsFileHandler.readStats(stats, SubmissionStatType.fromString(type.uppercase()))
        return submissionStatsService.saveAll(statsList)
    }

    suspend fun increment(
        type: String,
        statsFile: File,
    ): List<SubmissionStat> {
        val statsList = statsFileHandler.readStats(statsFile, SubmissionStatType.fromString(type.uppercase()))
        return submissionStatsService.incrementAll(statsList)
    }

    suspend fun calculateSubFilesSize(accNo: String): SubmissionStat {
        val sub = extSubmissionQueryService.getExtByAccNo(accNo, includeFileListFiles = true)
        logger.info { "${sub.accNo} ${sub.owner} Started calculating submission stats" }

        var subFilesSize = totalSize(sub)
        val subFilesSizeStat = submissionStatsService.save(SingleSubmissionStat(sub.accNo, subFilesSize, FILES_SIZE))
        logger.info { "${sub.accNo} ${sub.owner} Finished calculating submission stats. Files size: $subFilesSize" }
        return subFilesSizeStat
    }

    private suspend fun totalSize(submission: ExtSubmission): Long {
        var subFilesSize = 0L
        serializationService.filesFlow(submission).collect { subFilesSize = subFilesSize + it.size }
        return subFilesSize
    }
}
