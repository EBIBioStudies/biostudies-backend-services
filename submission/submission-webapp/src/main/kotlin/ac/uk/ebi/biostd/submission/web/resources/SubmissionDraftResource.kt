package ac.uk.ebi.biostd.submission.web.resources

import ac.uk.ebi.biostd.persistence.common.model.SubmissionDraft
import ac.uk.ebi.biostd.persistence.common.request.PaginationFilter
import ac.uk.ebi.biostd.submission.converters.BioUser
import ac.uk.ebi.biostd.submission.domain.service.SubmissionDraftService
import ac.uk.ebi.biostd.submission.web.model.OnBehalfRequest
import ac.uk.ebi.biostd.submission.web.model.SubmissionRequestParameters
import com.fasterxml.jackson.annotation.JsonRawValue
import com.fasterxml.jackson.annotation.JsonValue
import ebi.ac.uk.security.integration.model.api.SecurityUser
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

@RestController
@RequestMapping(value = ["submissions/drafts"], produces = [APPLICATION_JSON_VALUE])
@PreAuthorize("isAuthenticated()")
@Suppress("LongParameterList")
internal class SubmissionDraftResource(
    private val submissionDraftService: SubmissionDraftService,
) {
    @GetMapping
    @ResponseBody
    fun getSubmissionDrafts(
        @BioUser user: SecurityUser,
        @ModelAttribute filter: PaginationFilter,
    ): List<ResponseSubmissionDraft> =
        submissionDraftService.getActiveSubmissionDrafts(user.email, filter).map { it.asResponseDraft() }

    @GetMapping("/{key}")
    @ResponseBody
    fun getOrCreateSubmissionDraft(
        @BioUser user: SecurityUser,
        @PathVariable key: String,
    ): ResponseSubmissionDraft = submissionDraftService.getOrCreateSubmissionDraft(user.email, key).asResponseDraft()

    @GetMapping("/{key}/content")
    @ResponseBody
    fun getSubmissionDraftContent(
        @BioUser user: SecurityUser,
        @PathVariable key: String,
    ): ResponseSubmissionDraftContent =
        ResponseSubmissionDraftContent(submissionDraftService.getSubmissionDraftContent(user.email, key))

    @DeleteMapping("/{key}")
    fun deleteSubmissionDraft(
        @BioUser user: SecurityUser,
        @PathVariable key: String,
    ) = submissionDraftService.deleteSubmissionDraft(user.email, key)

    @PutMapping("/{key}")
    @ResponseBody
    fun updateSubmissionDraft(
        @BioUser user: SecurityUser,
        @RequestBody content: String,
        @PathVariable key: String,
    ): ResponseSubmissionDraft =
        submissionDraftService.updateSubmissionDraft(user.email, key, content).asResponseDraft()

    @PostMapping
    @ResponseBody
    fun createSubmissionDraft(
        @BioUser user: SecurityUser,
        @RequestBody content: String,
    ): ResponseSubmissionDraft = submissionDraftService.createSubmissionDraft(user.email, content).asResponseDraft()

    @PostMapping("/{key}/submit")
    fun submitDraft(
        @PathVariable key: String,
        @BioUser user: SecurityUser,
        onBehalfRequest: OnBehalfRequest?,
        @ModelAttribute parameters: SubmissionRequestParameters,
    ) {
        submissionDraftService.submitDraft(key, user, onBehalfRequest, parameters)
    }
}

internal class ResponseSubmissionDraft(val key: String, @JsonRawValue val content: String)

internal class ResponseSubmissionDraftContent(@JsonRawValue @JsonValue val value: String)

internal fun SubmissionDraft.asResponseDraft() = ResponseSubmissionDraft(key, content)
