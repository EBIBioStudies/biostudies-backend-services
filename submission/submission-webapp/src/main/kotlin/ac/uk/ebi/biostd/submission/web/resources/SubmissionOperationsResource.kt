package ac.uk.ebi.biostd.submission.web.resources

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.submission.converters.BioUser
import ac.uk.ebi.biostd.submission.domain.request.SubmissionRequestReleaser
import ac.uk.ebi.biostd.submission.web.handlers.SubmissionsWebHandler
import ebi.ac.uk.security.integration.model.api.SecurityUser
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/submissions")
@PreAuthorize("isAuthenticated()")
class SubmissionOperationsResource(
    private val submissionsWebHandler: SubmissionsWebHandler,
    private val submissionReleaser: SubmissionRequestReleaser,
    private val serializationService: SerializationService,
) {
    @PostMapping("/check-pagetab")
    suspend fun checkSubmission(
        @RequestBody body: String,
        @RequestParam(name = "source") source: String,
        @RequestParam(name = "target") target: String,
    ): String {
        val submission = serializationService.deserializeSubmission(body, SubFormat.fromString(source))
        return serializationService.serializeSubmission(submission, SubFormat.fromString(target))
    }

    @PostMapping("/ftp/generate")
    suspend fun generateFtpLinks(
        @RequestParam("accNo", required = true) accNo: String,
    ) {
        submissionReleaser.generateFtpLinks(accNo)
    }

    @DeleteMapping("/{accNo}")
    suspend fun deleteSubmission(
        @BioUser user: SecurityUser,
        @PathVariable accNo: String,
    ): Unit = submissionsWebHandler.deleteSubmission(accNo, user)

    @DeleteMapping
    suspend fun deleteSubmissions(
        @BioUser user: SecurityUser,
        @RequestParam submissions: List<String>,
    ): Unit = submissionsWebHandler.deleteSubmissions(submissions, user)
}
