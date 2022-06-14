package ac.uk.ebi.biostd.submission.web.resources

import ac.uk.ebi.biostd.persistence.common.model.BasicSubmission
import ac.uk.ebi.biostd.submission.converters.BioUser
import ac.uk.ebi.biostd.submission.domain.service.SubmissionService
import ac.uk.ebi.biostd.submission.web.handlers.SubmissionsWebHandler
import ac.uk.ebi.biostd.submission.web.model.SubmissionFilterRequest
import ac.uk.ebi.biostd.submission.web.model.asFilter
import ebi.ac.uk.api.dto.SubmissionDto
import ebi.ac.uk.model.constants.APPLICATION_JSON
import ebi.ac.uk.model.constants.TEXT_PLAIN
import ebi.ac.uk.model.constants.TEXT_XML
import ebi.ac.uk.security.integration.model.api.SecurityUser
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/submissions")
class SubmissionQueryResource(
    private val submissionService: SubmissionService,
    private val submissionsWebHandler: SubmissionsWebHandler,
) {
    @GetMapping("/{accNo}.json", produces = [APPLICATION_JSON])
    @ResponseBody
    fun asJson(@PathVariable accNo: String) = submissionService.getSubmissionAsJson(accNo)

    @GetMapping("/{accNo}.xml", produces = [TEXT_XML])
    fun asXml(@PathVariable accNo: String) = submissionService.getSubmissionAsXml(accNo)

    @GetMapping("/{accNo}.tsv", produces = [TEXT_PLAIN])
    fun asTsv(@PathVariable accNo: String) = submissionService.getSubmissionAsTsv(accNo)

    @GetMapping
    fun getSubmissions(
        @BioUser user: SecurityUser,
        @ModelAttribute request: SubmissionFilterRequest
    ): List<SubmissionDto> = submissionsWebHandler.getSubmissions(user, request.asFilter()).map { it.asDto() }

    private fun BasicSubmission.asDto() =
        SubmissionDto(
            accNo,
            title.orEmpty(),
            version,
            creationTime,
            modificationTime,
            releaseTime,
            method,
            status.value
        )
}
