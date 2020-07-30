package ac.uk.ebi.biostd.submission.web.resources.submit

import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.persistence.integration.FileMode
import ac.uk.ebi.biostd.submission.converters.BioUser
import ac.uk.ebi.biostd.submission.web.handlers.SubmitWebHandler
import ac.uk.ebi.biostd.submission.web.model.ContentSubmitWebRequest
import ac.uk.ebi.biostd.submission.web.model.OnBehalfRequest
import ebi.ac.uk.api.ON_BEHALF_PARAM
import ebi.ac.uk.api.REGISTER_PARAM
import ebi.ac.uk.api.TOKEN_HEADER
import ebi.ac.uk.api.USER_NAME_PARAM
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constants.ATTRIBUTES
import ebi.ac.uk.model.constants.FILE_MODE
import ebi.ac.uk.model.constants.SUBMISSION_TYPE
import ebi.ac.uk.model.constants.TEXT_PLAIN
import ebi.ac.uk.security.integration.model.api.SecurityUser
import io.swagger.annotations.Api
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiImplicitParams
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import springfox.documentation.annotations.ApiIgnore

@RestController
@RequestMapping("/submissions")
@PreAuthorize("isAuthenticated()")
@Api(tags = ["Submissions"])
@ApiIgnore
@Suppress("MaxLineLength")
class SubmitTsvResource(private val submitWebHandler: SubmitWebHandler) {

    @PostMapping(
        headers = ["$SUBMISSION_TYPE=$TEXT_PLAIN"],
        produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    @ApiOperation("Make a submission in TSV format")
    @ApiImplicitParams(value = [
        ApiImplicitParam(name = TOKEN_HEADER, value = "User auth token", required = true, paramType = "header"),
        ApiImplicitParam(name = ON_BEHALF_PARAM, value = "Submission owner", required = false),
        ApiImplicitParam(name = REGISTER_PARAM, value = "Register owner if does not exists", required = false),
        ApiImplicitParam(name = USER_NAME_PARAM, value = "Submission owner name. For register mode", required = false)
    ])
    fun submitTsv(
        @BioUser user: SecurityUser,

        onBehalfRequest: OnBehalfRequest?,

        @ApiParam(name = "fileMode", value = "File mode either copy/move")
        @RequestParam(FILE_MODE, defaultValue = "COPY") mode: FileMode,

        @ApiParam(name = "Attributes", value = "List of attributes to be added to the submission")
        @RequestParam(ATTRIBUTES, required = false) attributes: Map<String, String>?,

        @ApiParam(name = "Submission", value = "Submission page tab in TSV format")
        @RequestBody submission: String
    ): Submission {
        val request = ContentSubmitWebRequest(
            submission = submission,
            onBehalfRequest = onBehalfRequest,
            user = user,
            format = SubFormat.TSV,
            fileMode = mode,
            attrs = attributes.orEmpty(),
            files = emptyList()
        )
        return submitWebHandler.submit(request)
    }
}
