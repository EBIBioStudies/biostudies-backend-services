package ac.uk.ebi.biostd.submission.web.resources

import ac.uk.ebi.biostd.integration.SubFormat.Companion.JSON
import ac.uk.ebi.biostd.integration.SubFormat.Companion.JSON_PRETTY
import ac.uk.ebi.biostd.integration.SubFormat.Companion.TSV
import ac.uk.ebi.biostd.integration.SubFormat.Companion.XML
import ac.uk.ebi.biostd.submission.converters.BioUser
import ac.uk.ebi.biostd.submission.domain.service.TempFileGenerator
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
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.security.access.prepost.PreAuthorize
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
    private val submissionWebHandler: SubmissionWebHandler,
    private val tempFileGenerator: TempFileGenerator
) {
    @PostMapping(
        headers = ["$CONTENT_TYPE=$MULTIPART_FORM_DATA", "$SUBMISSION_TYPE=$APPLICATION_JSON_VALUE"],
        produces = [APPLICATION_JSON_VALUE])
    @ResponseBody
    fun submitMultipartJson(
        @BioUser user: SecurityUser,
        @RequestParam(SUBMISSION) submissionContent: String,
        @RequestParam(FILES) files: Array<MultipartFile>
    ) = submissionWebHandler.submit(user, tempFileGenerator.asFiles(files), submissionContent, JSON)

    @PostMapping(
        headers = ["$CONTENT_TYPE=$MULTIPART_FORM_DATA", "$SUBMISSION_TYPE=$TEXT_XML"],
        produces = [APPLICATION_JSON_VALUE])
    @ResponseBody
    fun submitMultipartXml(
        @BioUser user: SecurityUser,
        @RequestParam(SUBMISSION) submissionContent: String,
        @RequestParam(FILES) files: Array<MultipartFile>
    ): Submission = submissionWebHandler.submit(user, tempFileGenerator.asFiles(files), submissionContent, XML)

    @PostMapping(
        headers = ["$CONTENT_TYPE=$MULTIPART_FORM_DATA", "$SUBMISSION_TYPE=$TEXT_PLAIN"],
        produces = [APPLICATION_JSON_VALUE])
    @ResponseBody
    fun submitMultipartTsv(
        @BioUser user: SecurityUser,
        @RequestParam(SUBMISSION) submissionContent: String,
        @RequestParam(FILES) files: Array<MultipartFile>
    ): Submission = submissionWebHandler.submit(user, tempFileGenerator.asFiles(files), submissionContent, TSV)

    @PostMapping(
        value = ["/direct"],
        headers = ["$CONTENT_TYPE=$MULTIPART_FORM_DATA"],
        produces = [APPLICATION_JSON_VALUE])
    @ResponseBody
    fun submitFile(
        @BioUser user: SecurityUser,
        @RequestParam(SUBMISSION) file: MultipartFile,
        @RequestParam(FILES) files: Array<MultipartFile>,
        @RequestParam attributes: Map<String, String> = emptyMap()
    ): Submission =
        submissionWebHandler.submit(user, tempFileGenerator.asFile(file), tempFileGenerator.asFiles(files), attributes)

    @PostMapping(
        headers = ["$SUBMISSION_TYPE=$TEXT_XML"],
        produces = [APPLICATION_JSON_VALUE])
    @ResponseBody
    fun submitXml(@BioUser user: SecurityUser, @RequestBody submission: String): Submission =
        submissionWebHandler.submit(user, submission, XML)

    @PostMapping(
        headers = ["$SUBMISSION_TYPE=$TEXT_PLAIN"],
        produces = [APPLICATION_JSON_VALUE])
    @ResponseBody
    fun submitTsv(@BioUser user: SecurityUser, @RequestBody submission: String): Submission =
        submissionWebHandler.submit(user, submission, TSV)

    @PostMapping(
        headers = ["$SUBMISSION_TYPE=$APPLICATION_JSON"],
        produces = [APPLICATION_JSON_VALUE])
    @ResponseBody
    fun submitJson(@BioUser user: SecurityUser, @RequestBody submission: String): Submission =
        submissionWebHandler.submit(user, submission, JSON_PRETTY)

    @DeleteMapping("/{accNo}")
    fun deleteSubmission(@BioUser user: SecurityUser, @PathVariable accNo: String): Unit =
        submissionWebHandler.deleteSubmission(accNo, user)
}
