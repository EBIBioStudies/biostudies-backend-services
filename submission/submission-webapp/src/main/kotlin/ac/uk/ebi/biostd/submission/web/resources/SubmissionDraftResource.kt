package ac.uk.ebi.biostd.submission.web.resources

import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.request.PageRequest
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
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import java.time.OffsetDateTime

@RestController
@RequestMapping(value = ["submissions/drafts"], produces = [APPLICATION_JSON_VALUE])
@PreAuthorize("isAuthenticated()")
@Suppress("LongParameterList")
internal class SubmissionDraftResource(
    private val submitWebHandler: SubmitWebHandler,
    private val submitRequestBuilder: SubmitRequestBuilder,
    private val requestDraftService: SubmissionRequestDraftService,
) {
    @GetMapping
    @ResponseBody
    suspend fun getSubmissionDrafts(
        @BioUser user: SecurityUser,
        @ModelAttribute filter: PageRequest,
    ): Flow<ResponseSubmissionDraft> = requestDraftService.findActiveRequestDrafts(user.email, filter).map { it.asResponseDraft() }

    @GetMapping("/{accNo}")
    @ResponseBody
    suspend fun getOrCreateSubmissionDraft(
        @BioUser user: SecurityUser,
        @PathVariable accNo: String,
    ): ResponseSubmissionDraft = requestDraftService.getOrCreateRequestDraftFromSubmission(accNo, user.email).asResponseDraft()

    @GetMapping("/{accNo}/content")
    @ResponseBody
    suspend fun getSubmissionDraftContent(
        @BioUser user: SecurityUser,
        @PathVariable accNo: String,
    ): ResponseSubmissionDraftContent {
        val requestDraft = requestDraftService.getRequestDraft(accNo, user.email)
        return ResponseSubmissionDraftContent(requestDraft.draft)
    }

    @DeleteMapping("/{accNo}")
    suspend fun deleteSubmissionDraft(
        @BioUser user: SecurityUser,
        @PathVariable accNo: String,
    ) {
        requestDraftService.deleteRequestDraft(accNo, user.email)
    }

    @PutMapping("/{accNo}")
    @ResponseBody
    suspend fun updateSubmissionDraft(
        @BioUser user: SecurityUser,
        @RequestBody content: String,
        @PathVariable accNo: String,
    ): ResponseSubmissionDraft = requestDraftService.updateRequestDraft(accNo, user.email, content).asResponseDraft()

    @PostMapping
    @ResponseBody
    suspend fun createSubmissionDraft(
        @BioUser user: SecurityUser,
        @RequestBody content: String,
    ): ResponseSubmissionDraft = requestDraftService.createRequestDraft(content, user.email).asResponseDraft()

    @PostMapping("/{accNo}/submit")
    suspend fun submitDraft(
        @PathVariable accNo: String,
        @BioUser user: SecurityUser,
        onBehalfRequest: OnBehalfParameters?,
        @ModelAttribute parameters: SubmitParameters,
    ) {
        val buildRequest = SubmitBuilderRequest(user, onBehalfRequest, parameters)
        val request = submitRequestBuilder.buildDraftRequest(accNo, user.email, buildRequest)

        submitWebHandler.submitAsync(request)
    }

    @PostMapping("/{accNo}/submit/sync")
    suspend fun submitDraftSync(
        @PathVariable accNo: String,
        @BioUser user: SecurityUser,
        onBehalfRequest: OnBehalfParameters?,
        @ModelAttribute parameters: SubmitParameters,
    ): Submission {
        val buildRequest = SubmitBuilderRequest(user, onBehalfRequest, parameters)
        val request = submitRequestBuilder.buildDraftRequest(accNo, user.email, buildRequest)

        return submitWebHandler.submit(request)
    }

    private fun SubmissionRequest.asResponseDraft() = ResponseSubmissionDraft(accNo, draft, modificationTime)
}

internal class ResponseSubmissionDraft(
    val key: String,
    @JsonRawValue val content: String,
    val modificationTime: OffsetDateTime,
)

internal class ResponseSubmissionDraftContent(
    @JsonRawValue @JsonValue val value: String,
)
