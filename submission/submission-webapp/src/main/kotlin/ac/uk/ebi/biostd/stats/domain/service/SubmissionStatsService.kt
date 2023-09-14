package ac.uk.ebi.biostd.stats.domain.service

import ac.uk.ebi.biostd.persistence.common.model.SubmissionStat
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType.FILES_SIZE
import ac.uk.ebi.biostd.persistence.common.request.PageRequest
import ac.uk.ebi.biostd.persistence.common.service.StatsDataService
import ac.uk.ebi.biostd.persistence.doc.model.SingleSubmissionStat
import ac.uk.ebi.biostd.stats.web.handlers.StatsFileHandler
import ac.uk.ebi.biostd.submission.domain.helpers.TempFileGenerator
import ac.uk.ebi.biostd.submission.domain.service.ExtSubmissionQueryService
import kotlinx.coroutines.flow.Flow
import mu.KotlinLogging
import org.springframework.web.multipart.MultipartFile
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.fileSequence

private val logger = KotlinLogging.logger {}

class SubmissionStatsService(
    private val statsFileHandler: StatsFileHandler,
    private val tempFileGenerator: TempFileGenerator,
    private val submissionStatsService: StatsDataService,
    private val serializationService: ExtSerializationService,
    private val extSubmissionQueryService: ExtSubmissionQueryService,
) {
    suspend fun findByAccNo(
        accNo: String,
    ): List<SubmissionStat> = submissionStatsService.findByAccNo(accNo)

    fun findByType(
        type: String,
        filter: PageRequest,
    ): Flow<SubmissionStat> = submissionStatsService.findByType(SubmissionStatType.fromString(type.uppercase()), filter)

    suspend fun findByAccNoAndType(
        accNo: String,
        type: String,
    ): SubmissionStat =
        submissionStatsService.findByAccNoAndType(accNo, SubmissionStatType.fromString(type.uppercase()))

    suspend fun register(
        stat: SubmissionStat,
    ): SubmissionStat = submissionStatsService.save(stat)

    suspend fun register(type: String, stats: MultipartFile): List<SubmissionStat> {
        val statsFile = tempFileGenerator.asFile(stats)
        val statsList = statsFileHandler.readStats(statsFile, SubmissionStatType.fromString(type.uppercase()))

        return submissionStatsService.saveAll(statsList)
    }

    suspend fun increment(type: String, stats: MultipartFile): List<SubmissionStat> {
        val statsFile = tempFileGenerator.asFile(stats)
        val statsList = statsFileHandler.readStats(statsFile, SubmissionStatType.fromString(type.uppercase()))

        return submissionStatsService.incrementAll(statsList)
    }

    suspend fun calculateSubFilesSize(accNo: String): SubmissionStat {
        val sub = extSubmissionQueryService.getExtendedSubmission(accNo, includeFileListFiles = true)
        logger.info { "${sub.accNo} ${sub.owner} Started calculating submission stats" }

        val subFilesSize = serializationService.fileSequence(sub).sumOf { it.size }
        val subFilesSizeStat = submissionStatsService.save(SingleSubmissionStat(sub.accNo, subFilesSize, FILES_SIZE))

        logger.info { "${sub.accNo} ${sub.owner} Finished calculating submission stats. Files size: $subFilesSize" }
        return subFilesSizeStat
    }
}
