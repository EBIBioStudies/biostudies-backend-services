package ac.uk.ebi.biostd.stats.web

import ac.uk.ebi.biostd.persistence.filter.PaginationFilter
import ac.uk.ebi.biostd.stats.web.handlers.StatsFileHandler
import ac.uk.ebi.biostd.submission.domain.service.TempFileGenerator
import ebi.ac.uk.model.constants.MULTIPART_FORM_DATA
import io.swagger.annotations.Api
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiImplicitParams
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
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
import springfox.documentation.annotations.ApiIgnore
import uk.ac.ebi.stats.model.SubmissionStat
import uk.ac.ebi.stats.model.SubmissionStatType
import uk.ac.ebi.stats.service.SubmissionStatsService

@RestController
@RequestMapping("/stats")
@PreAuthorize("isAuthenticated()")
@Api(tags = ["Submission Stats"])
@ApiIgnore
class StatsResource(
    private val statsFileHandler: StatsFileHandler,
    private val tempFileGenerator: TempFileGenerator,
    private val submissionStatsService: SubmissionStatsService
) {
    @GetMapping("/{type}", produces = [APPLICATION_JSON_VALUE])
    @ResponseBody
    @ApiOperation("List all the registered submission stats of the given type")
    @ApiImplicitParams(value = [
        ApiImplicitParam(
            name = "X-SESSION-TOKEN",
            required = true,
            paramType = "header",
            value = "Authentication token"),
        ApiImplicitParam(
            name = "limit",
            paramType = "query",
            value = "Optional query parameter to set the maximum amount of stats in the response"),
        ApiImplicitParam(
            name = "offset",
            paramType = "query",
            value = "Optional query parameter to indicate from which stat should the response start")
    ])
    fun findByType(
        @ApiParam(name = "type", value = "The type of stat. It should be one of the following: views")
        @PathVariable type: String,

        @ModelAttribute filter: PaginationFilter
    ): List<SubmissionStat> = submissionStatsService.findByType(SubmissionStatType.valueOf(type.toUpperCase()), filter)

    @GetMapping("/{type}/{accNo}")
    @ResponseBody
    @ApiOperation("List all the registered submission stats of the given type for the given accession")
    @ApiImplicitParam(name = "X-SESSION-TOKEN", value = "Authentication token", required = true, paramType = "header")
    fun findByTypeAndAccNo(
        @ApiParam(name = "type", value = "The type of stat. It should be one of the following: views")
        @PathVariable type: String,

        @ApiParam(name = "accNo", value = "The submission accession number")
        @PathVariable accNo: String
    ): SubmissionStat = submissionStatsService.findByAccNoAndType(accNo, SubmissionStatType.valueOf(type.toUpperCase()))

    @PostMapping
    @ResponseBody
    @ApiOperation("Register a stat")
    @ApiImplicitParam(name = "X-SESSION-TOKEN", value = "Authentication token", required = true, paramType = "header")
    fun register(
        @ApiParam(name = "stat", value = "The stat to register")
        @RequestBody stat: SubmissionStat
    ): SubmissionStat = submissionStatsService.save(stat)

    @PostMapping("/{type}", headers = ["$CONTENT_TYPE=$MULTIPART_FORM_DATA"])
    @ResponseBody
    @ApiOperation("Register the stats contained in the given tab separated file")
    @ApiImplicitParam(name = "X-SESSION-TOKEN", value = "Authentication token", required = true, paramType = "header")
    fun register(
        @ApiParam(name = "type", value = "The type of stat. It should be one of the following: views")
        @PathVariable type: String,

        @ApiParam(name = "stats", value = "Tab separated file containing the stats to register")
        @RequestParam("stats") stats: MultipartFile
    ): List<SubmissionStat> {
        val statsFile = tempFileGenerator.asFile(stats)
        val statsList = statsFileHandler.readStats(statsFile, SubmissionStatType.valueOf(type.toUpperCase()))

        return submissionStatsService.saveAll(statsList)
    }

    @PostMapping("/{type}/increment", headers = ["$CONTENT_TYPE=$MULTIPART_FORM_DATA"])
    @ResponseBody
    @ApiOperation("Increment the value of the stats contained in the given tab separated file.")
    @ApiImplicitParam(name = "X-SESSION-TOKEN", value = "Authentication token", required = true, paramType = "header")
    fun increment(
        @ApiParam(name = "type", value = "The type of stat. It should be one of the following: views")
        @PathVariable type: String,

        @ApiParam(name = "stats", value = "Tab separated file containing the stats to increment")
        @RequestParam("stats") stats: MultipartFile
    ): List<SubmissionStat> {
        val statsFile = tempFileGenerator.asFile(stats)
        val statsList = statsFileHandler.readStats(statsFile, SubmissionStatType.valueOf(type.toUpperCase()))

        return submissionStatsService.incrementAll(statsList)
    }
}
