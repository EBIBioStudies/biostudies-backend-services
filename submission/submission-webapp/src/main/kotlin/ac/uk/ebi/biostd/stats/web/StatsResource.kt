package ac.uk.ebi.biostd.stats.web

import ac.uk.ebi.biostd.persistence.common.model.SubmissionStat
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType
import ac.uk.ebi.biostd.persistence.common.request.PaginationFilter
import ac.uk.ebi.biostd.persistence.common.service.StatsDataService
import ac.uk.ebi.biostd.stats.web.handlers.StatsFileHandler
import ac.uk.ebi.biostd.submission.domain.helpers.TempFileGenerator
import ebi.ac.uk.model.constants.MULTIPART_FORM_DATA
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/stats")
@PreAuthorize("isAuthenticated()")
class StatsResource(
    private val statsFileHandler: StatsFileHandler,
    private val tempFileGenerator: TempFileGenerator,
    private val submissionStatsService: StatsDataService
) {
    @GetMapping("/{type}", produces = [APPLICATION_JSON_VALUE])
    @ResponseBody
    fun findByType(
        @PathVariable type: String,
        @ModelAttribute filter: PaginationFilter
    ): List<SubmissionStat> = submissionStatsService.findByType(SubmissionStatType.valueOf(type.uppercase()), filter)

    @GetMapping("/{type}/{accNo}")
    @ResponseBody
    fun findByTypeAndAccNo(
        @PathVariable type: String,
        @PathVariable accNo: String
    ): SubmissionStat = submissionStatsService.findByAccNoAndType(accNo, SubmissionStatType.valueOf(type.uppercase()))

    @PostMapping("/{type}", headers = ["$CONTENT_TYPE=$MULTIPART_FORM_DATA"])
    @ResponseBody
    fun register(
        @PathVariable type: String,
        @RequestParam("stats") stats: MultipartFile
    ): List<SubmissionStat> {
        val statsFile = tempFileGenerator.asFile(stats)
        val statsList = statsFileHandler.readStats(statsFile, SubmissionStatType.valueOf(type.uppercase()))

        return submissionStatsService.saveAll(statsList)
    }

    @PostMapping("/{type}/increment", headers = ["$CONTENT_TYPE=$MULTIPART_FORM_DATA"])
    @ResponseBody
    fun increment(
        @PathVariable type: String,
        @RequestParam("stats") stats: MultipartFile
    ): List<SubmissionStat> {
        val statsFile = tempFileGenerator.asFile(stats)
        val statsList = statsFileHandler.readStats(statsFile, SubmissionStatType.valueOf(type.uppercase()))
        return submissionStatsService.incrementAll(statsList)
    }
}
