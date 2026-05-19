package ac.uk.ebi.biostd.submission.web.resources.submit.sync

import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.submission.converters.BioUser
import ac.uk.ebi.biostd.submission.web.handlers.SubmitBuilderRequest
import ac.uk.ebi.biostd.submission.web.handlers.SubmitRequestBuilder
import ac.uk.ebi.biostd.submission.web.handlers.SubmitWebHandler
import ebi.ac.uk.api.OnBehalfParameters
import ebi.ac.uk.api.SubmitParameters
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constants.APPLICATION_JSON
import ebi.ac.uk.model.constants.SUBMISSION_TYPE
import ebi.ac.uk.model.constants.TEXT_PLAIN
import ebi.ac.uk.security.integration.model.api.SecurityUser
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/submissions")
@PreAuthorize("isAuthenticated()")
@Tag(name = "Synchronous Submission", description = "Submit content and wait for validation and persistence to complete before returning.")
@Suppress("LongParameterList")
class SubmitResource(
    private val submitWebHandler: SubmitWebHandler,
    private val submitRequestBuilder: SubmitRequestBuilder,
) {
    @PostMapping(
        headers = ["$SUBMISSION_TYPE=$TEXT_PLAIN"],
        produces = [APPLICATION_JSON_VALUE],
    )
    @ResponseBody
    @Operation(
        summary = "Submit PageTab Synchronously",
        description =
            "Submit PageTab TSV content in the request body and wait for processing to complete. " +
                "Use this when the caller needs the resulting submission in the response.",
    )
    suspend fun submitTsv(
        @BioUser user: SecurityUser,
        onBehalfRequest: OnBehalfParameters?,
        @RequestBody submission: String,
        @ModelAttribute parameters: SubmitParameters,
    ): Submission {
        val buildRequest = SubmitBuilderRequest(user, onBehalfRequest, parameters)
        val request = submitRequestBuilder.buildContentRequest(submission, SubFormat.TSV, buildRequest)

        return submitWebHandler.submit(request)
    }

    @PostMapping(
        headers = ["$SUBMISSION_TYPE=$APPLICATION_JSON"],
        produces = [APPLICATION_JSON_VALUE],
    )
    @ResponseBody
    @Operation(
        summary = "Submit JSON Synchronously",
        description =
            "Submit BioStudies JSON content in the request body and wait for processing to complete. " +
                "Use this when the caller needs the resulting submission in the response.",
    )
    suspend fun submitJson(
        @BioUser user: SecurityUser,
        onBehalfRequest: OnBehalfParameters?,
        @RequestBody submission: String,
        @ModelAttribute parameters: SubmitParameters,
    ): Submission {
        val buildRequest = SubmitBuilderRequest(user, onBehalfRequest, parameters)
        val request = submitRequestBuilder.buildContentRequest(submission, SubFormat.JSON_PRETTY, buildRequest)

        return submitWebHandler.submit(request)
    }
}
