package ac.uk.ebi.biostd.submission.web.resources.submit.async

import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.submission.converters.BioUser
import ac.uk.ebi.biostd.submission.web.handlers.SubmitBuilderRequest
import ac.uk.ebi.biostd.submission.web.handlers.SubmitRequestBuilder
import ac.uk.ebi.biostd.submission.web.handlers.SubmitWebHandler
import ebi.ac.uk.api.OnBehalfParameters
import ebi.ac.uk.api.SubmitParameters
import ebi.ac.uk.model.SubmissionId
import ebi.ac.uk.model.constants.APPLICATION_JSON
import ebi.ac.uk.model.constants.SUBMISSION_TYPE
import ebi.ac.uk.model.constants.TEXT_PLAIN
import ebi.ac.uk.security.integration.model.api.SecurityUser
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/submissions/async")
@PreAuthorize("isAuthenticated()")
@Suppress("LongParameterList")
class SubmitAsyncResource(
    private val submitWebHandler: SubmitWebHandler,
    private val submitRequestBuilder: SubmitRequestBuilder,
) {
    @PostMapping(
        headers = ["$SUBMISSION_TYPE=$TEXT_PLAIN"],
        produces = [APPLICATION_JSON_VALUE],
    )
    @ResponseBody
    suspend fun submitTsv(
        @BioUser user: SecurityUser,
        onBehalfRequest: OnBehalfParameters?,
        @RequestBody submission: String,
        @ModelAttribute parameters: SubmitParameters,
    ): SubmissionId {
        val buildRequest = SubmitBuilderRequest(user, onBehalfRequest, parameters, asynchronous = true)
        val request = submitRequestBuilder.buildContentRequest(submission, SubFormat.TSV, buildRequest)
        return submitWebHandler.submitAsync(request)
    }

    @PostMapping(
        headers = ["$SUBMISSION_TYPE=$APPLICATION_JSON"],
        produces = [APPLICATION_JSON_VALUE],
    )
    @ResponseBody
    suspend fun submitJson(
        @BioUser user: SecurityUser,
        onBehalfRequest: OnBehalfParameters?,
        @RequestBody submission: String,
        @ModelAttribute parameters: SubmitParameters,
    ): SubmissionId {
        val buildRequest = SubmitBuilderRequest(user, onBehalfRequest, parameters, asynchronous = true)
        val request = submitRequestBuilder.buildContentRequest(submission, SubFormat.JSON, buildRequest)
        return submitWebHandler.submitAsync(request)
    }
}
