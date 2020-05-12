package ac.uk.ebi.biostd.submission.web.resources

import ac.uk.ebi.biostd.integration.SubFormat.Companion.JSON_PRETTY
import ac.uk.ebi.biostd.integration.SubFormat.Companion.TSV
import ac.uk.ebi.biostd.integration.SubFormat.Companion.XML
import ac.uk.ebi.biostd.persistence.integration.FileMode
import ac.uk.ebi.biostd.submission.converters.BioUser
import ac.uk.ebi.biostd.submission.web.handlers.SubmitWebHandler
import ac.uk.ebi.biostd.submission.web.model.ContentSubmitWebRequest
import ac.uk.ebi.biostd.submission.web.model.RefreshWebRequest
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constants.APPLICATION_JSON
import ebi.ac.uk.model.constants.ATTRIBUTES
import ebi.ac.uk.model.constants.FILE_MODE
import ebi.ac.uk.model.constants.SUBMISSION_TYPE
import ebi.ac.uk.model.constants.TEXT_PLAIN
import ebi.ac.uk.model.constants.TEXT_XML
import ebi.ac.uk.security.integration.model.api.SecurityUser
import io.swagger.annotations.Api
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/submissions")
@PreAuthorize("isAuthenticated()")
@Api(tags = ["Submissions"])
class SubmitResource(private val submitWebHandler: SubmitWebHandler) {

    @PostMapping(
        headers = ["$SUBMISSION_TYPE=$TEXT_XML"],
        produces = [APPLICATION_JSON_VALUE])
    @ResponseBody
    @ApiOperation("Make a submission in XML format")
    @ApiImplicitParam(name = "X-Session-Token", value = "User authentication token", required = true)
    fun submitXml(
        @BioUser user: SecurityUser,

        @ApiParam(name = "fileMode", value = "File mode either copy/move")
        @RequestParam(FILE_MODE, defaultValue = "COPY") mode: FileMode,

        @ApiParam(name = "Attributes", value = "List of attributes to be added to the submission")
        @RequestParam(ATTRIBUTES, required = false) attributes: Map<String, String>?,

        @ApiParam(name = "Submission", value = "Submission page tab in XML format")
        @RequestBody submission: String
    ): Submission {
        val request = ContentSubmitWebRequest(submission, user, XML, mode, attributes.orEmpty(), emptyList())
        return submitWebHandler.submit(request)
    }

    @PostMapping(
        headers = ["$SUBMISSION_TYPE=$TEXT_PLAIN"],
        produces = [APPLICATION_JSON_VALUE])
    @ResponseBody
    @ApiOperation("Make a submission in TSV format")
    @ApiImplicitParam(name = "X-Session-Token", value = "User authentication token", required = true)
    fun submitTsv(
        @BioUser user: SecurityUser,

        @ApiParam(name = "fileMode", value = "File mode either copy/move")
        @RequestParam(FILE_MODE, defaultValue = "COPY") mode: FileMode,

        @ApiParam(name = "Attributes", value = "List of attributes to be added to the submission")
        @RequestParam(ATTRIBUTES, required = false) attributes: Map<String, String>?,

        @ApiParam(name = "Submission", value = "Submission page tab in TSV format")
        @RequestBody submission: String
    ): Submission {
        val request = ContentSubmitWebRequest(submission, user, TSV, mode, attributes.orEmpty(), emptyList())
        return submitWebHandler.submit(request)
    }

    @PostMapping(
        headers = ["$SUBMISSION_TYPE=$APPLICATION_JSON"],
        produces = [APPLICATION_JSON_VALUE])
    @ResponseBody
    @ApiOperation("Make a submission in JSON format")
    @ApiImplicitParam(name = "X-Session-Token", value = "User authentication token", required = true)
    fun submitJson(
        @BioUser user: SecurityUser,

        @ApiParam(name = "fileMode", value = "File mode either copy/move")
        @RequestParam(FILE_MODE, defaultValue = "COPY") mode: FileMode,

        @ApiParam(name = "Attributes", value = "List of attributes to be added to the submission")
        @RequestParam(ATTRIBUTES, required = false) attributes: Map<String, String>?,

        @ApiParam(name = "Submission", value = "Submission page tab in JSON format")
        @RequestBody submission: String
    ): Submission {
        val request = ContentSubmitWebRequest(submission, user, JSON_PRETTY, mode, attributes.orEmpty(), emptyList())
        return submitWebHandler.submit(request)
    }

    @PostMapping("refresh/{accNo}")
    @ApiOperation("Update submission based on system db stored information")
    @ApiImplicitParam(
        name = "X-Session-Token",
        value = "User authentication token",
        required = true,
        paramType = "header")
    fun refreshSubmission(
        @BioUser user: SecurityUser,

        @ApiParam(name = "accNo", value = "The accession number of the submission to be refresh")
        @PathVariable accNo: String
    ): Submission {
        return submitWebHandler.refreshSubmission(RefreshWebRequest(accNo, user))
    }
}
