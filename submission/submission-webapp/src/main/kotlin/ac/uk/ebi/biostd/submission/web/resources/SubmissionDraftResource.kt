package ac.uk.ebi.biostd.submission.web.resources

import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.request.PageRequest
import ac.uk.ebi.biostd.persistence.doc.model.formatter
import ac.uk.ebi.biostd.submission.converters.BioUser
import ac.uk.ebi.biostd.submission.domain.service.SubmissionRequestDraftService
import ac.uk.ebi.biostd.submission.web.handlers.SubmitBuilderRequest
import ac.uk.ebi.biostd.submission.web.handlers.SubmitRequestBuilder
import ac.uk.ebi.biostd.submission.web.handlers.SubmitWebHandler
import com.fasterxml.jackson.annotation.JsonRawValue
import com.fasterxml.jackson.annotation.JsonValue
import ebi.ac.uk.api.OnBehalfParameters
import ebi.ac.uk.api.SubmitParameters
import ebi.ac.uk.model.Submission
import ebi.ac.uk.security.integration.model.api.SecurityUser
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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
import java.time.OffsetDateTime

@RestController
@RequestMapping(value = ["submissions/drafts"], produces = [APPLICATION_JSON_VALUE])
@PreAuthorize("isAuthenticated()")
@Tag(name = "Submission Drafts", description = "Create, list, update and submit submission drafts.")
@Suppress("LongParameterList")
class SubmissionDraftResource(
    private val submitWebHandler: SubmitWebHandler,
    private val submitRequestBuilder: SubmitRequestBuilder,
    private val requestDraftService: SubmissionRequestDraftService,
) {
    @GetMapping
    @ResponseBody
    @Operation(
        summary = "Search Submission Drafts",
        description = "Search for submission drafts belonging to the authenticated user.",
    )
    suspend fun getSubmissionDrafts(
        @Parameter(hidden = true) @BioUser user: SecurityUser,
        @ModelAttribute filter: PageRequest,
    ): Flow<ResponseSubmissionDraft> = requestDraftService.findActiveRequestDrafts(user.email, filter).map { it.asResponseDraft() }

    @GetMapping("/{accNo}")
    @ResponseBody
    @Operation(
        summary = "Get Submission Draft",
        description = "Get the submission draft with the specified accNo, creating one if it does not exist.",
    )
    suspend fun getOrCreateSubmissionDraft(
        @Parameter(hidden = true) @BioUser user: SecurityUser,
        @Parameter(description = "Submission accNo", required = true) @PathVariable accNo: String,
    ): ResponseSubmissionDraft = requestDraftService.getOrCreateRequestDraftFromSubmission(accNo, user.email).asResponseDraft()

    @GetMapping("/{accNo}/content")
    @ResponseBody
    @Operation(
        summary = "Get Submission Draft Content",
        description = "Get the raw content of the submission draft with the specified accNo.",
    )
    suspend fun getSubmissionDraftContent(
        @Parameter(hidden = true) @BioUser user: SecurityUser,
        @Parameter(description = "Submission accNo", required = true) @PathVariable accNo: String,
    ): ResponseSubmissionDraftContent {
        val requestDraft = requestDraftService.getRequestDraft(accNo, user.email)
        return ResponseSubmissionDraftContent(requestDraft.draft!!)
    }

    @DeleteMapping("/{accNo}")
    @Operation(
        summary = "Delete Submission Draft",
        description = "Delete the submission draft with the specified accNo.",
    )
    suspend fun deleteSubmissionDraft(
        @Parameter(hidden = true) @BioUser user: SecurityUser,
        @Parameter(description = "Submission accNo", required = true) @PathVariable accNo: String,
    ) {
        requestDraftService.deleteRequestDraft(accNo, user.email)
    }

    @PutMapping("/{accNo}")
    @ResponseBody
    @Operation(
        summary = "Update Submission Draft",
        description = "Replace the content of the submission draft with the specified accNo.",
    )
    suspend fun updateSubmissionDraft(
        @Parameter(hidden = true) @BioUser user: SecurityUser,
        @RequestBody content: String,
        @Parameter(description = "Submission accNo", required = true) @PathVariable accNo: String,
    ): ResponseSubmissionDraft = requestDraftService.updateRequestDraft(accNo, user.email, content).asResponseDraft()

    @PostMapping
    @ResponseBody
    @Operation(summary = "Create Submission Draft", description = "Create a new submission draft.")
    suspend fun createSubmissionDraft(
        @Parameter(hidden = true) @BioUser user: SecurityUser,
        @RequestBody content: String,
        @Parameter(description = "Existing submission accNo to attach the draft to") @RequestParam attachTo: String?,
    ): ResponseSubmissionDraft = requestDraftService.createRequestDraft(content, user.email, attachTo).asResponseDraft()

    @PostMapping("/{accNo}/submit")
    @Operation(
        summary = "Submit Draft (Async)",
        description =
            "Submit the draft with the given accNo asynchronously. The draft is deleted once the submission completes.",
    )
    suspend fun submitDraft(
        @Parameter(description = "Submission accNo", required = true) @PathVariable accNo: String,
        @Parameter(hidden = true) @BioUser user: SecurityUser,
        onBehalfRequest: OnBehalfParameters?,
        @ModelAttribute parameters: SubmitParameters,
    ) {
        val buildRequest = SubmitBuilderRequest(user, onBehalfRequest, parameters)
        val request = submitRequestBuilder.buildDraftRequest(accNo, user.email, buildRequest)
        submitWebHandler.submitAsync(request)
    }

    @PostMapping("/{accNo}/submit/sync")
    @Operation(
        summary = "Submit Draft (Sync)",
        description = "Submit the draft with the given accNo synchronously and return the resulting submission.",
    )
    suspend fun submitDraftSync(
        @Parameter(description = "Submission accNo", required = true) @PathVariable accNo: String,
        @Parameter(hidden = true) @BioUser user: SecurityUser,
        onBehalfRequest: OnBehalfParameters?,
        @ModelAttribute parameters: SubmitParameters,
    ): Submission {
        val buildRequest = SubmitBuilderRequest(user, onBehalfRequest, parameters)
        val request = submitRequestBuilder.buildDraftRequest(accNo, user.email, buildRequest)
        return submitWebHandler.submit(request)
    }

    private fun SubmissionRequest.asResponseDraft() =
        ResponseSubmissionDraft(
            key = accNo,
            displayKey = if (newSubmission) creationTime.format(formatter) else accNo,
            content = draft!!,
            newSubmission = newSubmission,
            modificationTime = modificationTime,
        )
}

@Schema(description = "Submission draft summary.")
class ResponseSubmissionDraft(
    @field:Schema(description = "Draft accession number", example = "TMP_2024-09-21T10:12:00.002Z")
    val key: String,
    @field:Schema(description = "Human-readable identifier for the draft", example = "S-BSST123")
    val displayKey: String,
    @field:Schema(description = "True if the draft is for a new submission, false if it edits an existing one")
    val newSubmission: Boolean,
    @field:Schema(
        description = "Raw page-tab JSON content of the draft",
        example = "{ \"type\": \"Submission\", \"section\": { \"type\": \"Study\" } }",
        type = "object",
    )
    @JsonRawValue
    val content: String,
    @field:Schema(description = "Last modification time", example = "2024-09-21T10:12:00.002Z")
    val modificationTime: OffsetDateTime,
)

@Schema(
    description = "Raw page-tab JSON content of a submission draft.",
    type = "object",
    example = "{ \"type\": \"Submission\", \"section\": { \"type\": \"Study\" } }",
)
class ResponseSubmissionDraftContent(
    @JsonRawValue
    @JsonValue
    val value: String,
)
