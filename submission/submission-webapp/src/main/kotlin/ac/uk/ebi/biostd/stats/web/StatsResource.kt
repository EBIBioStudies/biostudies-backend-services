package ac.uk.ebi.biostd.stats.web

import ac.uk.ebi.biostd.persistence.common.request.PageRequest
import ac.uk.ebi.biostd.submission.stats.service.SubmissionStatsService
import ebi.ac.uk.model.SubmissionStat
import ebi.ac.uk.model.UpdateResult
import ebi.ac.uk.model.constants.MULTIPART_FORM_DATA
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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
@Tag(name = "Statistics", description = "Submission statistics lookup and ingestion operations.")
class StatsResource(
    private val submissionStatsService: SubmissionStatsService,
    private val tmpFileGenerator: TempFileGenerator,
) {
    @Operation(summary = "Get all statistics for a submission")
    @GetMapping("/submission/{accNo}", produces = [APPLICATION_JSON_VALUE])
    @ResponseBody
    suspend fun findByAccNo(
        @PathVariable accNo: String,
    ): List<SubmissionStat> = submissionStatsService.findByAccNo(accNo).map { it.toStatDto() }

    @Operation(summary = "List statistics by type")
    @GetMapping("/{type}", produces = [APPLICATION_JSON_VALUE])
    @ResponseBody
    fun findByType(
        @PathVariable type: String,
        @ModelAttribute filter: PageRequest,
    ): Flow<SubmissionStat> = submissionStatsService.findByType(type, filter).map { it.toStatDto() }

    @Operation(summary = "Get one statistic for a submission")
    @GetMapping("/{type}/{accNo}")
    @ResponseBody
    suspend fun findByTypeAndAccNo(
        @PathVariable type: String,
        @PathVariable accNo: String,
    ): SubmissionStat = submissionStatsService.findByAccNoAndType(accNo, type).toStatDto()

    @Operation(summary = "Increment statistics from an uploaded file")
    @PostMapping("/{type}/increment", headers = ["$CONTENT_TYPE=$MULTIPART_FORM_DATA"])
    @ResponseBody
    suspend fun increment(
        @PathVariable type: String,
        @RequestParam("stats") stats: MultipartFile,
    ): UpdateResult {
        val statFile = tmpFileGenerator.asFile(stats)
        return submissionStatsService.increment(type, statFile)
    }
}
