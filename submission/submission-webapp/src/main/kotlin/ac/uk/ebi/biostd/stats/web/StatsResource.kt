package ac.uk.ebi.biostd.stats.web

import ac.uk.ebi.biostd.persistence.common.request.PaginationFilter
import ac.uk.ebi.biostd.stats.domain.service.SubmissionStatsService
import ac.uk.ebi.biostd.stats.web.mapping.toStat
import ac.uk.ebi.biostd.stats.web.mapping.toStatDto
import ac.uk.ebi.biostd.stats.web.model.SubmissionStatDto
import ebi.ac.uk.model.constants.MULTIPART_FORM_DATA
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
    private val submissionStatsService: SubmissionStatsService
) {
    @GetMapping("/submission/{accNo}", produces = [APPLICATION_JSON_VALUE])
    @ResponseBody
    fun findByAccNo(
        @PathVariable accNo: String
    ): List<SubmissionStatDto> = submissionStatsService.findByAccNo(accNo).map { it.toStatDto() }

    @GetMapping("/{type}", produces = [APPLICATION_JSON_VALUE])
    @ResponseBody
    fun findByType(
        @PathVariable type: String,
        @ModelAttribute filter: PaginationFilter
    ): List<SubmissionStatDto> = submissionStatsService.findByType(type, filter).map { it.toStatDto() }

    @GetMapping("/{type}/{accNo}")
    @ResponseBody
    fun findByTypeAndAccNo(
        @PathVariable type: String,
        @PathVariable accNo: String
    ): SubmissionStatDto = submissionStatsService.findByAccNoAndType(accNo, type).toStatDto()

    @PostMapping
    @ResponseBody
    fun register(
        @RequestBody stat: SubmissionStatDto
    ): SubmissionStatDto = submissionStatsService.register(stat.toStat()).toStatDto()

    @PostMapping("/{type}", headers = ["$CONTENT_TYPE=$MULTIPART_FORM_DATA"])
    @ResponseBody
    fun register(
        @PathVariable type: String,
        @RequestParam("stats") stats: MultipartFile
    ): List<SubmissionStatDto> = submissionStatsService.register(type, stats).map { it.toStatDto() }

    @PostMapping("/{type}/increment", headers = ["$CONTENT_TYPE=$MULTIPART_FORM_DATA"])
    @ResponseBody
    fun increment(
        @PathVariable type: String,
        @RequestParam("stats") stats: MultipartFile
    ): List<SubmissionStatDto> = submissionStatsService.increment(type, stats).map { it.toStatDto() }
}
