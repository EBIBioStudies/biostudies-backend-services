package ac.uk.ebi.biostd.stats.domain.service

import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType
import ac.uk.ebi.biostd.persistence.common.request.PaginationFilter
import ac.uk.ebi.biostd.persistence.common.service.StatsDataService
import ac.uk.ebi.biostd.stats.web.handlers.StatsFileHandler
import ac.uk.ebi.biostd.stats.web.mapping.toStat
import ac.uk.ebi.biostd.stats.web.mapping.toStatDto
import ac.uk.ebi.biostd.stats.web.model.SubmissionStatDto
import ac.uk.ebi.biostd.submission.domain.helpers.TempFileGenerator
import org.springframework.web.multipart.MultipartFile

class SubmissionStatsService(
    private val statsFileHandler: StatsFileHandler,
    private val tempFileGenerator: TempFileGenerator,
    private val submissionStatsService: StatsDataService,
) {
    fun findByAccNo(
        accNo: String,
    ): List<SubmissionStatDto> = submissionStatsService.findByAccNo(accNo).map { it.toStatDto() }

    fun findByType(
        type: String,
        filter: PaginationFilter,
    ): List<SubmissionStatDto> =
        submissionStatsService
            .findByType(SubmissionStatType.valueOf(type.uppercase()), filter)
            .map { it.toStatDto() }

    fun findByAccNoAndType(
        accNo: String,
        type: String,
    ): SubmissionStatDto =
        submissionStatsService
            .findByAccNoAndType(accNo, SubmissionStatType.valueOf(type.uppercase()))
            .toStatDto()

    fun register(
        stat: SubmissionStatDto
    ): SubmissionStatDto = submissionStatsService.save(stat.toStat()).toStatDto()

    fun register(type: String, stats: MultipartFile): List<SubmissionStatDto> {
        val statsFile = tempFileGenerator.asFile(stats)
        val statsList = statsFileHandler.readStats(statsFile, SubmissionStatType.valueOf(type.uppercase()))

        return submissionStatsService.saveAll(statsList).map { it.toStatDto() }
    }

    fun increment(type: String, stats: MultipartFile): List<SubmissionStatDto> {
        val statsFile = tempFileGenerator.asFile(stats)
        val statsList = statsFileHandler.readStats(statsFile, SubmissionStatType.valueOf(type.uppercase()))

        return submissionStatsService.incrementAll(statsList).map { it.toStatDto() }
    }
}
