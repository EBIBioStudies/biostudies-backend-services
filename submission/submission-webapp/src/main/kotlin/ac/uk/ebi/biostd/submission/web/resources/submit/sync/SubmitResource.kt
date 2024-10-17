package ac.uk.ebi.biostd.submission.web.resources.submit.sync

import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.integration.SubFormat.Companion.JSON
import ac.uk.ebi.biostd.submission.converters.BioUser
import ac.uk.ebi.biostd.submission.domain.submission.SubmissionQueryService
import ac.uk.ebi.biostd.submission.model.AcceptedSubmission
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
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/submissions")
@PreAuthorize("isAuthenticated()")
@Suppress("LongParameterList")
class SubmitResource(
    private val submitWebHandler: SubmitWebHandler,
    private val subQueryService: SubmissionQueryService,
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

    @PostMapping("/{accNo}/resubmit")
    suspend fun resubmit(
        @BioUser user: SecurityUser,
        @PathVariable accNo: String,
        onBehalfRequest: OnBehalfParameters?,
        @ModelAttribute parameters: SubmitParameters,
    ): AcceptedSubmission {
        val submission = subQueryService.getSubmission(accNo, JSON)
        val buildRequest = SubmitBuilderRequest(user, onBehalfRequest, parameters)
        val request = submitRequestBuilder.buildContentRequest(submission, JSON, buildRequest)

        return submitWebHandler.submitAsync(request)
    }
}
