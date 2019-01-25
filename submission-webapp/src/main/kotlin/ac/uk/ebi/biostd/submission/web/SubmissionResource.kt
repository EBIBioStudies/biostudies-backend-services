package ac.uk.ebi.biostd.submission.web

import ac.uk.ebi.biostd.submission.service.SubmissionService
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.User
import ebi.ac.uk.model.constants.JSON_TYPE
import ebi.ac.uk.model.constants.SUB_TYPE_HEADER
import ebi.ac.uk.model.constants.TSV_TYPE
import ebi.ac.uk.model.constants.XML_TYPE
import org.springframework.http.HttpHeaders
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/submissions")
@PreAuthorize("isAuthenticated()")
class SubmissionResource(
        private val submissionService: SubmissionService) {

    @GetMapping("/{accNo}.json",
            produces = [JSON_TYPE],
            headers = ["${HttpHeaders.CONTENT_TYPE}=$JSON_TYPE", "$SUB_TYPE_HEADER=$JSON_TYPE"])
    @ResponseBody
    fun asJson(@PathVariable accNo: String) = submissionService.getAsJson(accNo)


    @GetMapping("/{accNo}.xml", produces = [XML_TYPE])
    fun asXml(@PathVariable accNo: String) = submissionService.getAsXml(accNo)

    @GetMapping("/{accNo}.tsv", produces = [TSV_TYPE])
    fun asTsv(@PathVariable accNo: String) = submissionService.getAsTsv(accNo)

    @PostMapping
    @ResponseBody
    fun submit(@AuthenticationPrincipal user: User, @RequestBody submission: Submission) =
            submissionService.submit(submission, user)
}
