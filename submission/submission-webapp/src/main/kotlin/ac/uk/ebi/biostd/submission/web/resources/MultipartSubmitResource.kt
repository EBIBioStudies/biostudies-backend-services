package ac.uk.ebi.biostd.submission.web.resources

import ac.uk.ebi.biostd.integration.SubFormat.Companion.JSON
import ac.uk.ebi.biostd.integration.SubFormat.Companion.TSV
import ac.uk.ebi.biostd.integration.SubFormat.Companion.XML
import ac.uk.ebi.biostd.persistence.integration.FileMode
import ac.uk.ebi.biostd.submission.converters.BioUser
import ac.uk.ebi.biostd.submission.domain.service.TempFileGenerator
import ac.uk.ebi.biostd.submission.web.handlers.SubmitWebHandler
import ac.uk.ebi.biostd.submission.web.model.ContentSubmitWebRequest
import ac.uk.ebi.biostd.submission.web.model.FileSubmitWebRequest
import ac.uk.ebi.biostd.submission.web.model.OnBehalfRequest
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constants.ATTRIBUTES
import ebi.ac.uk.model.constants.FILES
import ebi.ac.uk.model.constants.FILE_MODE
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
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/submissions")
@PreAuthorize("isAuthenticated()")
@Api(tags = ["Submissions"])
class MultipartSubmitResource(
    private val submitWebHandler: SubmitWebHandler,
    private val tempFileGenerator: TempFileGenerator
) {
    @PostMapping(
        headers = ["$CONTENT_TYPE=$MULTIPART_FORM_DATA", "$SUBMISSION_TYPE=$APPLICATION_JSON_VALUE"],
        produces = [APPLICATION_JSON_VALUE])
    @ResponseBody
    @ApiOperation("Make a submission using a JSON file. The given files will override the ones in the user folder")
    @Suppress("LongParameterList")
    @ApiImplicitParam(name = "X-Session-Token", value = "User authentication token", required = true)
    fun submitMultipartJson(
        @BioUser user: SecurityUser,

        onBehalfRequest: OnBehalfRequest?,

        @ApiParam(name = "submission", value = "File containing the submission page tab in JSON format")
        @RequestParam(SUBMISSION) content: String,

        @ApiParam(name = "fileMode", value = "File mode either copy/move")
        @RequestParam(FILE_MODE, defaultValue = "COPY") mode: FileMode,

        @ApiParam(name = "Files", value = "List of files to be used in the submission")
        @RequestParam(FILES, required = false) files: Array<MultipartFile>?,

        @ApiParam(name = "Attributes", value = "List of attributes to be added to the submission")
        @RequestParam(ATTRIBUTES, required = false) attributes: Map<String, String>?
    ): Submission {
        val tempFiles = tempFileGenerator.asFiles(files.orEmpty())
        val contentWebRequest = ContentSubmitWebRequest(
            submission = content,
            onBehalfRequest = onBehalfRequest,
            user = user,
            format = JSON,
            fileMode = mode,
            attrs = attributes.orEmpty(),
            files = tempFiles
        )
        return submitWebHandler.submit(contentWebRequest)
    }

    @PostMapping(
        headers = ["$CONTENT_TYPE=$MULTIPART_FORM_DATA", "$SUBMISSION_TYPE=$TEXT_XML"],
        produces = [APPLICATION_JSON_VALUE])
    @ResponseBody
    @ApiOperation("Make a submission using a XML file. The given files will override the ones in the user folder")
    @ApiImplicitParam(name = "X-Session-Token", value = "User authentication token", required = true)
    @Suppress("LongParameterList")
    fun submitMultipartXml(
        @BioUser user: SecurityUser,

        onBehalfRequest: OnBehalfRequest?,

        @ApiParam(name = "Submission", value = "File containing the submission page tab in XML format")
        @RequestParam(SUBMISSION) content: String,

        @ApiParam(name = "fileMode", value = "File mode either copy/move")
        @RequestParam(FILE_MODE, defaultValue = "COPY") mode: FileMode,

        @ApiParam(name = "Attributes", value = "List of attributes to be added to the submission")
        @RequestParam(ATTRIBUTES, required = false) attributes: Map<String, String>?,

        @ApiParam(name = "Files", value = "List of files to be used in the submission")
        @RequestParam(FILES, required = false) files: Array<MultipartFile>?
    ): Submission {
        val tempFiles = tempFileGenerator.asFiles(files.orEmpty())
        val contentWebRequest = ContentSubmitWebRequest(
            submission = content,
            onBehalfRequest = onBehalfRequest,
            user = user,
            format = XML,
            fileMode = mode,
            attrs = attributes.orEmpty(),
            files = tempFiles
        )
        return submitWebHandler.submit(contentWebRequest)
    }

    @PostMapping(
        headers = ["$CONTENT_TYPE=$MULTIPART_FORM_DATA", "$SUBMISSION_TYPE=$TEXT_PLAIN"],
        produces = [APPLICATION_JSON_VALUE])
    @ResponseBody
    @ApiOperation("Make a submission using a TSV file. The given files will override the ones in the user folder")
    @ApiImplicitParam(name = "X-Session-Token", value = "User authentication token", required = true)
    @Suppress("LongParameterList")
    fun submitMultipartTsv(
        @BioUser user: SecurityUser,

        onBehalfRequest: OnBehalfRequest?,

        @ApiParam(name = "Submission", value = "File containing the submission page tab in TSV format")
        @RequestParam(SUBMISSION) content: String,

        @ApiParam(name = "fileMode", value = "File mode either copy/move")
        @RequestParam(FILE_MODE, defaultValue = "COPY") mode: FileMode,

        @ApiParam(name = "Attributes", value = "List of attributes to be added to the submission")
        @RequestParam(ATTRIBUTES, required = false) attributes: Map<String, String>?,

        @ApiParam(name = "Files", value = "List of files to be used in the submission")
        @RequestParam(FILES, required = false) files: Array<MultipartFile>?
    ): Submission {
        val tempFiles = tempFileGenerator.asFiles(files.orEmpty())
        val contentWebRequest = ContentSubmitWebRequest(
            submission = content,
            onBehalfRequest = onBehalfRequest,
            user = user,
            format = TSV,
            fileMode = mode,
            attrs = attributes.orEmpty(),
            files = tempFiles
        )
        return submitWebHandler.submit(contentWebRequest)
    }

    @PostMapping(
        value = ["/direct"],
        headers = ["$CONTENT_TYPE=$MULTIPART_FORM_DATA"],
        produces = [APPLICATION_JSON_VALUE])
    @ResponseBody
    @ApiOperation("Make a submission using a file. The given files will override the ones in the user folder")
    @ApiImplicitParam(name = "X-Session-Token", value = "User authentication token", required = true)
    @Suppress("LongParameterList")
    fun submitFile(
        @BioUser user: SecurityUser,

        onBehalfRequest: OnBehalfRequest?,

        @ApiParam(
            name = "Submission",
            value = "File containing the submission page tab. The format will be detected based on the file extension")
        @RequestParam(SUBMISSION) file: MultipartFile,

        @ApiParam(name = "Files", value = "List of files to be used in the submission")
        @RequestParam(FILES) files: Array<MultipartFile>,

        @ApiParam(name = "fileMode", value = "File mode either copy/move")
        @RequestParam(FILE_MODE, defaultValue = "COPY") mode: FileMode,

        @ApiParam(name = "Attributes", value = "List of attributes to be added to the submission")
        @RequestParam attributes: Map<String, String> = emptyMap()
    ): Submission {
        val tempFiles = tempFileGenerator.asFiles(files)
        val subFile = tempFileGenerator.asFile(file)
        val contentWebRequest = FileSubmitWebRequest(
            submission = subFile,
            onBehalfRequest = onBehalfRequest,
            user = user,
            format = TSV,
            fileMode = mode,
            attrs = attributes,
            files = tempFiles
        )
        return submitWebHandler.submit(contentWebRequest)
    }
}
