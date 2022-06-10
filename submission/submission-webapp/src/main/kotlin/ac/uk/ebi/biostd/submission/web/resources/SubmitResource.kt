package ac.uk.ebi.biostd.submission.web.resources

import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.submission.converters.BioUser
import ac.uk.ebi.biostd.submission.web.handlers.SubmitBuilderRequest
import ac.uk.ebi.biostd.submission.web.handlers.SubmitRequestBuilder
import ac.uk.ebi.biostd.submission.web.handlers.SubmitWebHandler
import ac.uk.ebi.biostd.submission.web.model.OnBehalfRequest
import ac.uk.ebi.biostd.submission.web.model.SubmissionRequestParameters
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constants.APPLICATION_JSON
import ebi.ac.uk.model.constants.SUBMISSION_TYPE
import ebi.ac.uk.model.constants.TEXT_PLAIN
import ebi.ac.uk.model.constants.TEXT_XML
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
@RequestMapping("/submissions")
@PreAuthorize("isAuthenticated()")
@Suppress("LongParameterList")
class SubmitResource(
    private val submitWebHandler: SubmitWebHandler,
    private val submitRequestBuilder: SubmitRequestBuilder,
) {
    @PostMapping(
        headers = ["$SUBMISSION_TYPE=$TEXT_XML"],
        produces = [APPLICATION_JSON_VALUE]
    )
    @ResponseBody
    fun submitXml(
        @BioUser user: SecurityUser,
        onBehalfRequest: OnBehalfRequest?,
        @RequestBody submission: String,
        @ModelAttribute parameters: SubmissionRequestParameters,
    ): Submission {
        val buildRequest = SubmitBuilderRequest(user, onBehalfRequest, SubFormat.XML, emptyArray(), parameters)
        val request = submitRequestBuilder.buildContentRequest(submission, buildRequest)

        return submitWebHandler.submit(request)
    }

    @PostMapping(
        headers = ["$SUBMISSION_TYPE=$TEXT_PLAIN"],
        produces = [APPLICATION_JSON_VALUE]
    )
    @ResponseBody
    fun submitTsv(
        @BioUser user: SecurityUser,
        onBehalfRequest: OnBehalfRequest?,
        @RequestBody submission: String,
        @ModelAttribute parameters: SubmissionRequestParameters,
    ): Submission {
        val buildRequest = SubmitBuilderRequest(user, onBehalfRequest, SubFormat.TSV, emptyArray(), parameters)
        val request = submitRequestBuilder.buildContentRequest(submission, buildRequest)

        return submitWebHandler.submit(request)
    }

    @PostMapping(
        headers = ["$SUBMISSION_TYPE=$APPLICATION_JSON"],
        produces = [APPLICATION_JSON_VALUE]
    )
    @ResponseBody
    fun submitJson(
        @BioUser user: SecurityUser,
        onBehalfRequest: OnBehalfRequest?,
        @RequestBody submission: String,
        @ModelAttribute parameters: SubmissionRequestParameters,
    ): Submission {
        val buildRequest = SubmitBuilderRequest(user, onBehalfRequest, SubFormat.JSON_PRETTY, emptyArray(), parameters)
        val request = submitRequestBuilder.buildContentRequest(submission, buildRequest)

        return submitWebHandler.submit(request)
    }
}
