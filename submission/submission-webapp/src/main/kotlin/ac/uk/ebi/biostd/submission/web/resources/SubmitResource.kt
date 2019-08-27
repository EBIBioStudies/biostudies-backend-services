package ac.uk.ebi.biostd.submission.web.resources

import ac.uk.ebi.biostd.integration.SubFormat.JSON
import ac.uk.ebi.biostd.integration.SubFormat.TSV
import ac.uk.ebi.biostd.integration.SubFormat.XML
import ac.uk.ebi.biostd.submission.web.handlers.SubmissionWebHandler
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constants.APPLICATION_JSON
import ebi.ac.uk.model.constants.FILES
import ebi.ac.uk.model.constants.MULTIPART_FORM_DATA
import ebi.ac.uk.model.constants.SUBMISSION
import ebi.ac.uk.model.constants.SUBMISSION_TYPE
import ebi.ac.uk.model.constants.TEXT_PLAIN
import ebi.ac.uk.model.constants.TEXT_XML
import ebi.ac.uk.security.integration.model.api.SecurityUser
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/submissions")
@PreAuthorize("isAuthenticated()")
class SubmitResource(private val submissionWebHandler: SubmissionWebHandler) {
    @PostMapping(headers = ["$CONTENT_TYPE=$MULTIPART_FORM_DATA", "$SUBMISSION_TYPE=$APPLICATION_JSON"])
    @ResponseBody
    fun submitJson(
        @AuthenticationPrincipal user: SecurityUser,
        @RequestParam(SUBMISSION) submissionContent: String,
        @RequestParam(FILES) files: Array<MultipartFile>
    ) = submissionWebHandler.submit(user, files, submissionContent, JSON)

    @PostMapping(headers = ["$CONTENT_TYPE=$MULTIPART_FORM_DATA", "$SUBMISSION_TYPE=$TEXT_XML"])
    @ResponseBody
    fun submitXml(
        @AuthenticationPrincipal user: SecurityUser,
        @RequestParam(SUBMISSION) submissionContent: String,
        @RequestParam(FILES) files: Array<MultipartFile>
    ): Submission = submissionWebHandler.submit(user, files, submissionContent, XML)

    @PostMapping(headers = ["$CONTENT_TYPE=$MULTIPART_FORM_DATA", "$SUBMISSION_TYPE=$TEXT_PLAIN"])
    @ResponseBody
    fun submitTsv(
        @AuthenticationPrincipal user: SecurityUser,
        @RequestParam(SUBMISSION) submissionContent: String,
        @RequestParam(FILES) files: Array<MultipartFile>
    ): Submission = submissionWebHandler.submit(user, files, submissionContent, TSV)

    @PostMapping(value = ["/direct"], headers = ["$CONTENT_TYPE=$MULTIPART_FORM_DATA"])
    @ResponseBody
    fun submitFile(
        @AuthenticationPrincipal user: SecurityUser,
        @RequestParam(SUBMISSION) file: MultipartFile,
        @RequestParam(FILES) files: Array<MultipartFile>
    ): Submission = submissionWebHandler.submit(user, file, files)

    @PostMapping(headers = ["$SUBMISSION_TYPE=$TEXT_XML"])
    @ResponseBody
    fun submitXml(@AuthenticationPrincipal user: SecurityUser, @RequestBody submission: String): Submission =
        submissionWebHandler.submit(user, submission, XML)

    @PostMapping(headers = ["$SUBMISSION_TYPE=$TEXT_PLAIN"])
    @ResponseBody
    fun submitTsv(@AuthenticationPrincipal user: SecurityUser, @RequestBody submission: String): Submission =
        submissionWebHandler.submit(user, submission, TSV)

    @PostMapping(headers = ["$SUBMISSION_TYPE=$APPLICATION_JSON"])
    @ResponseBody
    fun submitJson(@AuthenticationPrincipal user: SecurityUser, @RequestBody submission: String): Submission =
        submissionWebHandler.submit(user, submission, JSON)

    @DeleteMapping("/{accNo}")
    fun deleteSubmission(@AuthenticationPrincipal user: SecurityUser, @PathVariable accNo: String) =
        submissionWebHandler.deleteSubmission(accNo, user)
}
