package ac.uk.ebi.biostd.stats.web

import ac.uk.ebi.biostd.persistence.common.request.PageRequest
import ac.uk.ebi.biostd.submission.stats.SubmissionStatsService
import ebi.ac.uk.model.SubmissionStat
import ebi.ac.uk.model.constants.MULTIPART_FORM_DATA
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/stats")
@PreAuthorize("isAuthenticated()")
class StatsResource(
    private val submissionStatsService: SubmissionStatsService,
    private val tmpFileGenerator: TempFileGenerator,
) {
    @GetMapping("/submission/{accNo}", produces = [APPLICATION_JSON_VALUE])
    @ResponseBody
    suspend fun findByAccNo(
        @PathVariable accNo: String,
    ): List<SubmissionStat> = submissionStatsService.findByAccNo(accNo).map { it.toStatDto() }

    @GetMapping("/{type}", produces = [APPLICATION_JSON_VALUE])
    @ResponseBody
    fun findByType(
        @PathVariable type: String,
        @ModelAttribute filter: PageRequest,
    ): Flow<SubmissionStat> = submissionStatsService.findByType(type, filter).map { it.toStatDto() }

    @GetMapping("/{type}/{accNo}")
    @ResponseBody
    suspend fun findByTypeAndAccNo(
        @PathVariable type: String,
        @PathVariable accNo: String,
    ): SubmissionStat = submissionStatsService.findByAccNoAndType(accNo, type).toStatDto()

    @PostMapping
    @ResponseBody
    suspend fun register(
        @RequestBody stat: SubmissionStat,
    ): SubmissionStat = submissionStatsService.register(stat.toStat()).toStatDto()

    @PostMapping("/{type}", headers = ["$CONTENT_TYPE=$MULTIPART_FORM_DATA"])
    @ResponseBody
    suspend fun register(
        @PathVariable type: String,
        @RequestParam("stats") stats: MultipartFile,
    ): List<SubmissionStat> {
        val statFile = tmpFileGenerator.asFile(stats)
        return submissionStatsService.register(type, statFile).map { it.toStatDto() }
    }

    @PostMapping("/{type}/increment", headers = ["$CONTENT_TYPE=$MULTIPART_FORM_DATA"])
    @ResponseBody
    suspend fun increment(
        @PathVariable type: String,
        @RequestParam("stats") stats: MultipartFile,
    ): List<SubmissionStat> {
        val statFile = tmpFileGenerator.asFile(stats)
        return submissionStatsService.increment(type, statFile).map { it.toStatDto() }
    }

    @PostMapping("/submission/{accNo}")
    @ResponseBody
    suspend fun calculateSubStats(
        @PathVariable accNo: String,
    ): List<SubmissionStat> = submissionStatsService.calculateStats(accNo).map { it.toStatDto() }
}
