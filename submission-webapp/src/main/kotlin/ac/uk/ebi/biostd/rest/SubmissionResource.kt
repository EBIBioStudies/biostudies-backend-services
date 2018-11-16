package ac.uk.ebi.biostd.rest

import ac.uk.ebi.biostd.service.SubmissionService
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.User
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
class SubmissionResource(private val submissionService: SubmissionService) {

    @GetMapping("/{accNo}.json", produces = ["application/json; charset=utf-8"])
    @ResponseBody
    fun asJson(@PathVariable accNo: String): String {
        return submissionService.getSubmissionAsJson(accNo)
    }

    @GetMapping("/{accNo}.xml", produces = ["application/xml; charset=utf-8"])
    fun asXml(@PathVariable accNo: String): String {
        return submissionService.getSubmissionAsXml(accNo)
    }

    @GetMapping("/{accNo}.tsv", produces = ["text/plain; charset=utf-8"])
    fun asTsv(@PathVariable accNo: String): String {
        return submissionService.getSubmissionAsTsv(accNo)
    }

    @PostMapping
    fun submit(@RequestBody submission: Submission, @AuthenticationPrincipal user: User) {
        submissionService.submitSubmission(submission, user)
    }
}
