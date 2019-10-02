package ac.uk.ebi.biostd.submission.web.resources

import ac.uk.ebi.biostd.persistence.util.SubmissionFilter
import ac.uk.ebi.biostd.submission.domain.service.SubmissionService
import ac.uk.ebi.biostd.submission.web.handlers.SubmissionWebHandler
import ebi.ac.uk.api.dto.SubmissionDto
import ebi.ac.uk.model.constants.APPLICATION_JSON
import ebi.ac.uk.model.constants.SUBMISSION_TYPE
import ebi.ac.uk.model.constants.TEXT_PLAIN
import ebi.ac.uk.model.constants.TEXT_XML
import ebi.ac.uk.security.integration.model.api.SecurityUser
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/submissions")
@PreAuthorize("isAuthenticated()")
class SubmissionResource(
    private val submissionService: SubmissionService,
    private val submissionWebHandler: SubmissionWebHandler
) {
    @GetMapping("/{accNo}.json",
        produces = [APPLICATION_JSON],
        headers = ["$CONTENT_TYPE=$APPLICATION_JSON", "$SUBMISSION_TYPE=$APPLICATION_JSON"])
    @ResponseBody
    fun asJson(@PathVariable accNo: String) = submissionService.getSubmissionAsJson(accNo)

    @GetMapping("/{accNo}.xml", produces = [TEXT_XML])
    fun asXml(@PathVariable accNo: String) = submissionService.getSubmissionAsXml(accNo)

    @GetMapping("/{accNo}.tsv", produces = [TEXT_PLAIN])
    fun asTsv(@PathVariable accNo: String) = submissionService.getSubmissionAsTsv(accNo)

    @GetMapping
    fun getSubmissions(
        @ModelAttribute filter: SubmissionFilter,
        @AuthenticationPrincipal user: SecurityUser
    ): List<SubmissionDto> = submissionWebHandler.getSubmissions(user, filter)
        .map { SubmissionDto(it.accNo, it.title, it.creationTime, it.modificationTime, it.releaseTime) }
}
