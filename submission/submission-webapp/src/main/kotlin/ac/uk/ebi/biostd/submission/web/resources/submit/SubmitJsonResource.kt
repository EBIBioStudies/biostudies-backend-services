package ac.uk.ebi.biostd.submission.web.resources.submit

import ac.uk.ebi.biostd.integration.SubFormat.Companion.JSON_PRETTY
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
import ebi.ac.uk.model.constants.APPLICATION_JSON
import ebi.ac.uk.model.constants.ATTRIBUTES
import ebi.ac.uk.model.constants.FILE_MODE
import ebi.ac.uk.model.constants.SUBMISSION_TYPE
import ebi.ac.uk.security.integration.model.api.SecurityUser
import io.swagger.annotations.Api
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiImplicitParams
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.security.access.prepost.PreAuthorize
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
@Suppress("MaxLineLength")
class SubmitJsonResource(private val submitWebHandler: SubmitWebHandler) {

    @PostMapping(
        headers = ["$SUBMISSION_TYPE=$APPLICATION_JSON"],
        produces = [APPLICATION_JSON_VALUE])
    @ResponseBody
    @ApiOperation(value = "Make a submission in JSON format", nickname = "submitJson")
    @ApiImplicitParams(value = [
        ApiImplicitParam(name = SUBMISSION_TYPE, allowableValues = APPLICATION_JSON, required = true, paramType = "header", readOnly = true),
        ApiImplicitParam(name = TOKEN_HEADER, value = "User auth token", required = true, paramType = "header"),
        ApiImplicitParam(name = ON_BEHALF_PARAM, value = "Submission owner user email.", required = false),
        ApiImplicitParam(name = REGISTER_PARAM, value = "Register owner if does not exists (true|false)", required = false),
        ApiImplicitParam(name = USER_NAME_PARAM, value = "Submission owner name. (For registration mode)", required = false)
    ])
    fun submitJson(
        @BioUser user: SecurityUser,

        onBehalfRequest: OnBehalfRequest?,

        @ApiParam(name = "fileMode", hidden = true, value = "File mode either copy/move")
        @RequestParam(FILE_MODE, defaultValue = "COPY") mode: FileMode,

        @ApiParam(name = "Attributes", hidden = true, value = "Map of attributes to be added to the submission")
        @RequestParam(ATTRIBUTES, required = false) attributes: Map<String, String>?,

        @ApiParam(
            name = "Submission",
            value = "Submission page tab in JSON format as specified in https://ebibiostudies.github.io/page-tab-specification/")
        @RequestBody submission: String
    ): Submission {
        val request = ContentSubmitWebRequest(
            submission = submission,
            onBehalfRequest = onBehalfRequest,
            user = user,
            format = JSON_PRETTY,
            fileMode = mode,
            attrs = attributes.orEmpty(),
            files = emptyList()
        )
        return submitWebHandler.submit(request)
    }
}
