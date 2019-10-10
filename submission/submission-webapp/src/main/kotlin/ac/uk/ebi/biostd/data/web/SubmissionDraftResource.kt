package ac.uk.ebi.biostd.data.web

import ac.uk.ebi.biostd.data.service.SubmissionDraftService
import com.fasterxml.jackson.annotation.JsonRawValue
import com.fasterxml.jackson.annotation.JsonValue
import ebi.ac.uk.model.SubmissionDraftKey
import ebi.ac.uk.security.integration.model.api.SecurityUser
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
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
class SubmissionDraftResource(private val subDraftService: SubmissionDraftService) {
    @GetMapping(value = ["/{accNo}"])
    @ResponseBody
    fun getDraftSubmission(@AuthenticationPrincipal user: SecurityUser, @PathVariable accNo: String): SubmissionDraft =
        SubmissionDraft(subDraftService.getSubmissionDraft(user.id, accNo).data)

    @GetMapping(params = ["searchText"])
    @ResponseBody
    fun searchDraftSubmission(@AuthenticationPrincipal user: SecurityUser, @RequestParam searchText: String):
        List<String> = subDraftService.searchSubmissionsDraft(user.id, searchText).map { it.data }

    @GetMapping(params = ["!searchText"])
    @ResponseBody
    fun searchDraftSubmission(@AuthenticationPrincipal user: SecurityUser): List<SubmissionDraft> =
        subDraftService.getSubmissionsDraft(user.id).map { SubmissionDraft(it.data) }

    @DeleteMapping("/{accNo}")
    fun deleteDraftSubmission(@AuthenticationPrincipal user: SecurityUser, @PathVariable accNo: String): Unit =
        subDraftService.deleteSubmissionDraft(user.id, accNo)

    @PutMapping("/{accNo}")
    @ResponseBody
    fun updateDraftSubmission(
        @AuthenticationPrincipal user: SecurityUser,
        @RequestBody content: String,
        @PathVariable accNo: String
    ): SubmissionDraft = SubmissionDraft(subDraftService.updateSubmissionDraft(user.id, accNo, content).data)

    @PostMapping
    @ResponseBody
    fun createDraftSubmission(
        @AuthenticationPrincipal user: SecurityUser,
        @RequestBody content: String
    ): SubmissionDraftKey = SubmissionDraftKey(subDraftService.createSubmissionDraft(user.id, content).key)
}

class SubmissionDraft(private val value: String) {
    @JsonValue
    @JsonRawValue
    fun value(): String = value
}

