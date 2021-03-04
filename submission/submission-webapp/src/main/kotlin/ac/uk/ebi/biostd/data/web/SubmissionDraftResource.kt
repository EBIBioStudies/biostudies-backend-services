package ac.uk.ebi.biostd.data.web

import ac.uk.ebi.biostd.integration.SubFormat.Companion.JSON_PRETTY
import ac.uk.ebi.biostd.persistence.common.request.PaginationFilter
import ac.uk.ebi.biostd.persistence.common.service.SubmissionDraftService
import ac.uk.ebi.biostd.submission.converters.BioUser
import ac.uk.ebi.biostd.submission.web.handlers.SubmitWebHandler
import ac.uk.ebi.biostd.submission.web.model.ContentSubmitWebRequest
import ac.uk.ebi.biostd.submission.web.model.OnBehalfRequest
import com.fasterxml.jackson.annotation.JsonRawValue
import com.fasterxml.jackson.annotation.JsonValue
import ebi.ac.uk.extended.model.FileMode
import ebi.ac.uk.model.SubmissionDraft
import ebi.ac.uk.model.constants.ATTRIBUTES
import ebi.ac.uk.model.constants.FILE_MODE
import ebi.ac.uk.security.integration.model.api.SecurityUser
import io.swagger.annotations.Api
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiImplicitParams
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(value = ["submissions/drafts"], produces = [APPLICATION_JSON_VALUE])
@PreAuthorize("isAuthenticated()")
@Api(tags = ["Submission Drafts"])
internal class SubmissionDraftResource(
    private val submitWebHandler: SubmitWebHandler,
    private val draftService: SubmissionDraftService
) {
    @GetMapping
    @ResponseBody
    @ApiOperation("Get the submission drafts that matches the given filter")
    @ApiImplicitParams(
        value = [
            ApiImplicitParam(name = "X-Session-Token", value = "User authentication token", required = true),
            ApiImplicitParam(
                name = "limit",
                value = "Optional query parameter used to set the maximum amount of drafts in the response"
            ),
            ApiImplicitParam(
                name = "offset",
                value = "Optional query parameter used to indicate from which submission should the response start"
            )
        ]
    )
    fun getDraftSubmissions(
        @BioUser user: SecurityUser,
        @ModelAttribute filter: PaginationFilter
    ): List<ResponseSubmissionDraft> = draftService.getSubmissionsDraft(user.email, filter).map { it.asResponseDraft() }

    @GetMapping("/{key}")
    @ResponseBody
    @ApiOperation("Get the submission drafts that matches the given key and filter")
    @ApiImplicitParams(
        value = [
            ApiImplicitParam(name = "X-Session-Token", value = "User authentication token", required = true),
            ApiImplicitParam(
                name = "limit",
                value = "Optional query parameter used to set the maximum amount of drafts in the response"
            ),
            ApiImplicitParam(
                name = "offset",
                value = "Optional query parameter used to indicate from which submission should the response start"
            )
        ]
    )
    fun getDraftSubmission(
        @BioUser user: SecurityUser,
        @ModelAttribute filter: PaginationFilter,

        @ApiParam(name = "Key", value = "The submission draft key")
        @PathVariable key: String
    ): ResponseSubmissionDraft = draftService.getSubmissionDraft(user.email, key).asResponseDraft()

    @GetMapping("/{key}/content")
    @ResponseBody
    @ApiOperation("Get the content of the submission draft with the given key")
    @ApiImplicitParam(name = "X-Session-Token", value = "User authentication token", required = true)
    fun getDraftSubmissionContent(
        @BioUser user: SecurityUser,

        @ApiParam(name = "Key", value = "The submission draft key")
        @PathVariable key: String
    ): ResponseSubmissionDraftContent =
        ResponseSubmissionDraftContent(draftService.getSubmissionDraft(user.email, key).content)

    @DeleteMapping("/{key}")
    @ApiOperation("Delete the submission draft with the given key")
    @ApiImplicitParam(name = "X-Session-Token", value = "User authentication token", required = true)
    fun deleteDraftSubmission(
        @BioUser user: SecurityUser,

        @ApiParam(name = "Key", value = "The submission draft key")
        @PathVariable key: String
    ) = draftService.deleteSubmissionDraft(user.email, key)

    @PutMapping("/{key}")
    @ResponseBody
    @ApiOperation("Update the submission draft with the given key")
    @ApiImplicitParam(name = "X-Session-Token", value = "User authentication token", required = true)
    fun updateSubmissionDraft(
        @BioUser user: SecurityUser,

        @ApiParam(name = "Content", value = "The new content for the submission draft")
        @RequestBody content: String,

        @ApiParam(name = "Key", value = "The submission draft key")
        @PathVariable key: String
    ): ResponseSubmissionDraft = draftService.updateSubmissionDraft(user.email, key, content).asResponseDraft()

    @PostMapping
    @ResponseBody
    @ApiOperation("Create a submission draft")
    @ApiImplicitParam(name = "X-Session-Token", value = "User authentication token", required = true)
    fun createDraftSubmission(
        @BioUser user: SecurityUser,

        @ApiParam(name = "Content", value = "The content for the submission draft")
        @RequestBody content: String
    ): ResponseSubmissionDraft = draftService.createSubmissionDraft(user.email, content).asResponseDraft()

    @PostMapping("/{key}/submit")
    fun submitDraft(
        @PathVariable key: String,
        @BioUser user: SecurityUser,
        onBehalfRequest: OnBehalfRequest?,
        @RequestParam(FILE_MODE, defaultValue = "COPY") mode: FileMode,
        @RequestParam(ATTRIBUTES, required = false) attributes: Map<String, String>?
    ) {
        val submission = draftService.getSubmissionDraft(user.email, key).content
        val request = ContentSubmitWebRequest(
            submission = submission,
            draftKey = key,
            onBehalfRequest = onBehalfRequest,
            user = user,
            format = JSON_PRETTY,
            fileMode = mode,
            attrs = attributes.orEmpty(),
            files = emptyList())

        submitWebHandler.submitAsync(request)
    }
}

internal class ResponseSubmissionDraft(val key: String, @JsonRawValue val content: String)
internal class ResponseSubmissionDraftContent(@JsonRawValue @JsonValue val value: String)

internal fun SubmissionDraft.asResponseDraft() = ResponseSubmissionDraft(key, content)
