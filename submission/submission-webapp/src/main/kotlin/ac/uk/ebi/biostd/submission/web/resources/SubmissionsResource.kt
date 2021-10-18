package ac.uk.ebi.biostd.submission.web.resources

import ac.uk.ebi.biostd.submission.domain.service.SubmissionService
import ebi.ac.uk.model.constants.APPLICATION_JSON
import ebi.ac.uk.model.constants.TEXT_PLAIN
import ebi.ac.uk.model.constants.TEXT_XML
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/submissions")
@PreAuthorize("isAuthenticated()")
class SubmissionsResource(private val submissionService: SubmissionService) {
    @GetMapping("/{accNo}.json", produces = [APPLICATION_JSON])
    @ResponseBody
    fun asJson(@PathVariable accNo: String) = submissionService.getSubmissionAsJson(accNo)

    @GetMapping("/{accNo}.xml", produces = [TEXT_XML])
    fun asXml(@PathVariable accNo: String) = submissionService.getSubmissionAsXml(accNo)

    @GetMapping("/{accNo}.tsv", produces = [TEXT_PLAIN])
    fun asTsv(@PathVariable accNo: String) = submissionService.getSubmissionAsTsv(accNo)
}
