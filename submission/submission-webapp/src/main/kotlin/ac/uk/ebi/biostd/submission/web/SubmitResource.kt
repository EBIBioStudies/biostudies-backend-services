package ac.uk.ebi.biostd.submission.web

import ac.uk.ebi.biostd.integration.ISerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.integration.SubFormat.JSON
import ac.uk.ebi.biostd.integration.SubFormat.TSV
import ac.uk.ebi.biostd.integration.SubFormat.XML
import ac.uk.ebi.biostd.submission.model.UserSource
import ac.uk.ebi.biostd.submission.service.SubmissionService
import ac.uk.ebi.biostd.submission.service.TempFileGenerator
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.User
import ebi.ac.uk.model.constants.APPLICATION_JSON
import ebi.ac.uk.model.constants.FILES
import ebi.ac.uk.model.constants.MULTIPART_FORM_DATA
import ebi.ac.uk.model.constants.SUBMISSION
import ebi.ac.uk.model.constants.SUBMISSION_TYPE
import ebi.ac.uk.model.constants.TEXT_PLAIN
import ebi.ac.uk.model.constants.TEXT_XML
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
class SubmitResource(
    private val submissionService: SubmissionService,
    private val tempFileGenerator: TempFileGenerator,
    private val serializationService: ISerializationService
) {

    @PostMapping(headers = ["$CONTENT_TYPE=$MULTIPART_FORM_DATA", "$SUBMISSION_TYPE=$APPLICATION_JSON"])
    @ResponseBody
    fun submitJson(
        @AuthenticationPrincipal user: User,
        @RequestParam(FILES) files: Array<MultipartFile>,
        @RequestParam(SUBMISSION) submissionContent: String
    ) = submit(files, user, submissionContent, JSON)

    @PostMapping(headers = ["$CONTENT_TYPE=$MULTIPART_FORM_DATA", "$SUBMISSION_TYPE=$TEXT_XML"])
    @ResponseBody
    fun submitXml(
        @AuthenticationPrincipal user: User,
        @RequestParam(FILES) files: Array<MultipartFile>,
        @RequestParam(SUBMISSION) submissionContent: String
    ): Submission = submit(files, user, submissionContent, XML)

    @PostMapping(headers = ["$CONTENT_TYPE=$MULTIPART_FORM_DATA", "$SUBMISSION_TYPE=$TEXT_PLAIN"])
    @ResponseBody
    fun submitTsv(
        @AuthenticationPrincipal user: User,
        @RequestParam(FILES) files: Array<MultipartFile>,
        @RequestParam(SUBMISSION) submissionContent: String
    ): Submission = submit(files, user, submissionContent, TSV)

    private fun submit(files: Array<MultipartFile>, user: User, content: String, format: SubFormat): Submission {
        val fileSource = UserSource(tempFileGenerator.asFiles(files), user.magicFolder)
        val submission = serializationService.deserializeSubmission(content, format, fileSource)
        return submissionService.submit(submission, user, fileSource)
    }

    @PostMapping(headers = ["$SUBMISSION_TYPE=$TEXT_XML"])
    @ResponseBody
    fun submitXml(@AuthenticationPrincipal user: User, @RequestBody submission: String): Submission =
        submit(user, submission, XML)

    @PostMapping(headers = ["$SUBMISSION_TYPE=$TEXT_PLAIN"])
    @ResponseBody
    fun submitTsv(@AuthenticationPrincipal user: User, @RequestBody submission: String): Submission =
        submit(user, submission, TSV)

    @ResponseBody
    fun submitJson(@AuthenticationPrincipal user: User, @RequestBody submission: String): Submission =
        submit(user, submission, JSON)

    @DeleteMapping("/{accNo}")
    fun deleteSubmission(@AuthenticationPrincipal user: User, @PathVariable accNo: String) =
        submissionService.deleteSubmission(accNo, user)

    private fun submit(user: User, content: String, format: SubFormat): Submission {
        val fileSource = UserSource(emptyList(), user.magicFolder)
        val submission = serializationService.deserializeSubmission(content, format, fileSource)
        return submissionService.submit(submission, user, fileSource)
    }
}
