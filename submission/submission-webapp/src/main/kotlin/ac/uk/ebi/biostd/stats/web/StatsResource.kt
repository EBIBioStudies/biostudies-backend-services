package ac.uk.ebi.biostd.stats.web

import ac.uk.ebi.biostd.stats.web.handlers.StatsFileHandler
import ac.uk.ebi.biostd.submission.domain.service.TempFileGenerator
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import uk.ac.ebi.stats.model.SubmissionStat
import uk.ac.ebi.stats.model.SubmissionStatType
import uk.ac.ebi.stats.service.SubmissionStatsService

@RestController
@RequestMapping("/stats")
class StatsResource(
    private val statsFileHandler: StatsFileHandler,
    private val tempFileGenerator: TempFileGenerator,
    private val submissionStatsService: SubmissionStatsService
) {
    @GetMapping("/{type}")
    fun findByType(
        @PathVariable type: String
    ): List<SubmissionStat> = submissionStatsService.findByType(SubmissionStatType.valueOf(type))

    @GetMapping("/{type}/{accNo}")
    fun findByType(
        @PathVariable type: String,
        @PathVariable accNo: String
    ): SubmissionStat = submissionStatsService.findByAccNoAndType(accNo, SubmissionStatType.valueOf(type))

    @PostMapping
    fun register(
        @RequestBody submissionStat: SubmissionStat
    ): SubmissionStat = submissionStatsService.save(submissionStat)

    @PatchMapping
    fun update(
        @RequestBody submissionStat: SubmissionStat
    ): SubmissionStat = submissionStatsService.update(submissionStat)

    @PostMapping("/{type}/bulk")
    fun bulkRegister(
        @PathVariable type: String,
        @RequestParam("stats") stats: MultipartFile
    ): List<SubmissionStat> {
        val statsFile = tempFileGenerator.asFile(stats)
        val statsList = statsFileHandler.readBulkStats(statsFile, SubmissionStatType.valueOf(type))

        return statsList.map { submissionStatsService.upsert(it) }
    }
}
