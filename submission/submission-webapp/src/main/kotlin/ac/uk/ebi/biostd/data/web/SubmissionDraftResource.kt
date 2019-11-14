package ac.uk.ebi.biostd.data.web

import ac.uk.ebi.biostd.data.service.SubmissionDraftService
import ac.uk.ebi.biostd.persistence.filter.PaginationFilter
import com.fasterxml.jackson.annotation.JsonRawValue
import com.fasterxml.jackson.annotation.JsonValue
import ebi.ac.uk.security.integration.model.api.SecurityUser
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
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
internal class SubmissionDraftResource(private val subDraftService: SubmissionDraftService) {
    @GetMapping
    @ResponseBody
    fun getDraftSubmissions(
        @AuthenticationPrincipal user: SecurityUser,
        @ModelAttribute filter: PaginationFilter
    ): List<SubmissionDraft> =
        subDraftService.getSubmissionsDraft(user.id, filter).map { SubmissionDraft(it.key, it.data) }

    @GetMapping("/{key}")
    @ResponseBody
    fun getDraftSubmission(
        @AuthenticationPrincipal user: SecurityUser,
        @ModelAttribute filter: PaginationFilter,
        @PathVariable key: String
    ): SubmissionDraft {
        val draft = subDraftService.getSubmissionDraft(user.id, key)
        return SubmissionDraft(draft.key, draft.data)
    }

    @GetMapping("/{key}/content")
    @ResponseBody
    fun getDraftSubmissionContent(@AuthenticationPrincipal user: SecurityUser, @PathVariable key: String):
        SubmissionDraftContent = SubmissionDraftContent(subDraftService.getSubmissionDraft(user.id, key).data)

    @DeleteMapping("/{key}")
    fun deleteDraftSubmission(@AuthenticationPrincipal user: SecurityUser, @PathVariable key: String): Unit =
        subDraftService.deleteSubmissionDraft(user.id, key)

    @PutMapping("/{key}")
    @ResponseBody
    fun updateDraftSubmission(
        @AuthenticationPrincipal user: SecurityUser,
        @RequestBody content: String,
        @PathVariable key: String
    ) {
        subDraftService.updateSubmissionDraft(user.id, key, content)
    }

    @PostMapping
    @ResponseBody
    fun createDraftSubmission(
        @AuthenticationPrincipal user: SecurityUser,
        @RequestBody content: String
    ): SubmissionDraft {
        val draft = subDraftService.createSubmissionDraft(user.id, content)
        return SubmissionDraft(draft.key, draft.data)
    }
}

internal class SubmissionDraft(val key: String, @JsonRawValue val content: String)
internal class SubmissionDraftContent(@JsonRawValue @JsonValue val value: String)
