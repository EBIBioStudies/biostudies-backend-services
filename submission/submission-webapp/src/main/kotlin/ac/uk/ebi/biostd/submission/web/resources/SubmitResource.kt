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
import io.swagger.annotations.Api
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
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
@Api(tags = ["Submissions"])
class SubmitResource(
    private val submissionWebHandler: SubmissionWebHandler,
    private val tempFileGenerator: TempFileGenerator
) {
    @PostMapping(
        headers = ["$CONTENT_TYPE=$MULTIPART_FORM_DATA", "$SUBMISSION_TYPE=$APPLICATION_JSON_VALUE"],
        produces = [APPLICATION_JSON_VALUE])
    @ResponseBody
    @ApiOperation("Make a submission using a JSON file. The given files will override the ones in the user folder")
    @ApiImplicitParam(name = "X-Session-Token", value = "The authentication token", required = true)
    fun submitMultipartJson(
        @BioUser user: SecurityUser,

        @ApiParam(name = "Submission", value = "File containing the submission page tab in JSON format")
        @RequestParam(SUBMISSION) submissionContent: String,

        @ApiParam(name = "Files", value = "List of files to be used in the submission")
        @RequestParam(FILES) files: Array<MultipartFile>
    ) = submissionWebHandler.submit(user, tempFileGenerator.asFiles(files), submissionContent, JSON)

    @PostMapping(
        headers = ["$CONTENT_TYPE=$MULTIPART_FORM_DATA", "$SUBMISSION_TYPE=$TEXT_XML"],
        produces = [APPLICATION_JSON_VALUE])
    @ResponseBody
    @ApiOperation("Make a submission using a XML file. The given files will override the ones in the user folder")
    @ApiImplicitParam(name = "X-Session-Token", value = "The authentication token", required = true)
    fun submitMultipartXml(
        @BioUser user: SecurityUser,

        @ApiParam(name = "Submission", value = "File containing the submission page tab in XML format")
        @RequestParam(SUBMISSION) submissionContent: String,

        @ApiParam(name = "Files", value = "List of files to be used in the submission")
        @RequestParam(FILES) files: Array<MultipartFile>
    ): Submission = submissionWebHandler.submit(user, tempFileGenerator.asFiles(files), submissionContent, XML)

    @PostMapping(
        headers = ["$CONTENT_TYPE=$MULTIPART_FORM_DATA", "$SUBMISSION_TYPE=$TEXT_PLAIN"],
        produces = [APPLICATION_JSON_VALUE])
    @ResponseBody
    @ApiOperation("Make a submission using a TSV file. The given files will override the ones in the user folder")
    @ApiImplicitParam(name = "X-Session-Token", value = "The authentication token", required = true)
    fun submitMultipartTsv(
        @BioUser user: SecurityUser,

        @ApiParam(name = "Submission", value = "File containing the submission page tab in TSV format")
        @RequestParam(SUBMISSION) submissionContent: String,

        @ApiParam(name = "Files", value = "List of files to be used in the submission")
        @RequestParam(FILES) files: Array<MultipartFile>
    ): Submission = submissionWebHandler.submit(user, tempFileGenerator.asFiles(files), submissionContent, TSV)

    @PostMapping(
        value = ["/direct"],
        headers = ["$CONTENT_TYPE=$MULTIPART_FORM_DATA"],
        produces = [APPLICATION_JSON_VALUE])
    @ResponseBody
    @ApiOperation("Make a submission using a file. The given files will override the ones in the user folder")
    @ApiImplicitParam(name = "X-Session-Token", value = "The authentication token", required = true)
    fun submitFile(
        @BioUser user: SecurityUser,

        @ApiParam(
            name = "Submission",
            value = "File containing the submission page tab. The format will be detected based on the file extension")
        @RequestParam(SUBMISSION) file: MultipartFile,

        @ApiParam(name = "Files", value = "List of files to be used in the submission")
        @RequestParam(FILES) files: Array<MultipartFile>
    ): Submission =
        submissionWebHandler.submit(user, tempFileGenerator.asFile(file), tempFileGenerator.asFiles(files))

    @PostMapping(
        headers = ["$SUBMISSION_TYPE=$TEXT_XML"],
        produces = [APPLICATION_JSON_VALUE])
    @ResponseBody
    @ApiOperation("Make a submission in XML format")
    @ApiImplicitParam(name = "X-Session-Token", value = "The authentication token", required = true)
    fun submitXml(
        @BioUser user: SecurityUser,

        @ApiParam(name = "Submission", value = "Submission page tab in XML format")
        @RequestBody submission: String
    ): Submission = submissionWebHandler.submit(user, submission, XML)

    @PostMapping(
        headers = ["$SUBMISSION_TYPE=$TEXT_PLAIN"],
        produces = [APPLICATION_JSON_VALUE])
    @ResponseBody
    @ApiOperation("Make a submission in TSV format")
    @ApiImplicitParam(name = "X-Session-Token", value = "The authentication token", required = true)
    fun submitTsv(
        @BioUser user: SecurityUser,

        @ApiParam(name = "Submission", value = "Submission page tab in TSV format")
        @RequestBody submission: String
    ): Submission = submissionWebHandler.submit(user, submission, TSV)

    @PostMapping(
        headers = ["$SUBMISSION_TYPE=$APPLICATION_JSON"],
        produces = [APPLICATION_JSON_VALUE])
    @ResponseBody
    @ApiOperation("Make a submission in JSON format")
    @ApiImplicitParam(name = "X-Session-Token", value = "The authentication token", required = true)
    fun submitJson(
        @BioUser user: SecurityUser,

        @ApiParam(name = "Submission", value = "Submission page tab in JSON format")
        @RequestBody submission: String
    ): Submission = submissionWebHandler.submit(user, submission, JSON_PRETTY)

    @DeleteMapping("/{accNo}")
    @ApiOperation("Delete the submission with the given accession number")
    @ApiImplicitParam(name = "X-Session-Token", value = "The authentication token", required = true)
    fun deleteSubmission(
        @BioUser user: SecurityUser,

        @ApiParam(name = "AccNo", value = "The accession number of the submission to be deleted")
        @PathVariable accNo: String
    ): Unit = submissionWebHandler.deleteSubmission(accNo, user)
}
