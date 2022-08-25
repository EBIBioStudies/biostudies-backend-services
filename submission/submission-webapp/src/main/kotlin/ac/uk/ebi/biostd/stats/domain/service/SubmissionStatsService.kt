package ac.uk.ebi.biostd.stats.domain.service

import ac.uk.ebi.biostd.persistence.common.model.SubmissionStat
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType
import ac.uk.ebi.biostd.persistence.common.request.PaginationFilter
import ac.uk.ebi.biostd.persistence.common.service.StatsDataService
import ac.uk.ebi.biostd.stats.web.handlers.StatsFileHandler
import ac.uk.ebi.biostd.submission.domain.helpers.TempFileGenerator
import org.springframework.web.multipart.MultipartFile

// TODO add tests

class SubmissionStatsService(
    private val statsFileHandler: StatsFileHandler,
    private val tempFileGenerator: TempFileGenerator,
    private val submissionStatsService: StatsDataService,
) {
    fun findByAccNo(
        accNo: String,
    ): List<SubmissionStat> = submissionStatsService.findByAccNo(accNo)

    fun findByType(
        type: String,
        filter: PaginationFilter,
    ): List<SubmissionStat> = submissionStatsService.findByType(SubmissionStatType.fromString(type.uppercase()), filter)

    fun findByAccNoAndType(
        accNo: String,
        type: String,
    ): SubmissionStat = submissionStatsService.findByAccNoAndType(accNo, SubmissionStatType.fromString(type.uppercase()))

    fun register(
        stat: SubmissionStat
    ): SubmissionStat = submissionStatsService.save(stat)

    fun register(type: String, stats: MultipartFile): List<SubmissionStat> {
        val statsFile = tempFileGenerator.asFile(stats)
        val statsList = statsFileHandler.readStats(statsFile, SubmissionStatType.fromString(type.uppercase()))

        return submissionStatsService.saveAll(statsList)
    }

    fun increment(type: String, stats: MultipartFile): List<SubmissionStat> {
        val statsFile = tempFileGenerator.asFile(stats)
        val statsList = statsFileHandler.readStats(statsFile, SubmissionStatType.fromString(type.uppercase()))

        return submissionStatsService.incrementAll(statsList)
    }
}
