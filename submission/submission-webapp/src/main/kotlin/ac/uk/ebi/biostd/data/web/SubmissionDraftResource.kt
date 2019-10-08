package ac.uk.ebi.biostd.data.web

import ac.uk.ebi.biostd.data.service.SubmissionDraftService
import ac.uk.ebi.biostd.persistence.model.UserData
import ebi.ac.uk.model.constants.APPLICATION_JSON
import ebi.ac.uk.security.integration.model.api.SecurityUser
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
@RequestMapping("submissions/drafts")
@PreAuthorize("isAuthenticated()")
class SubmissionDraftResource(private val subDraftService: SubmissionDraftService) {
    @GetMapping(value = ["/{accNo}"], produces = [APPLICATION_JSON])
    @ResponseBody
    fun getDraftSubmission(@AuthenticationPrincipal user: SecurityUser, @PathVariable accNo: String): String =
        subDraftService.getSubmissionDraft(user.id, accNo).data

    @GetMapping(params = ["searchText"], produces = [APPLICATION_JSON])
    @ResponseBody
    fun searchTmpSubmission(@AuthenticationPrincipal user: SecurityUser, @RequestParam searchText: String):
        List<String> = subDraftService.searchSubmissionsDraft(user.id, searchText).map { it.data }

    @GetMapping(params = ["!searchText"], produces = [APPLICATION_JSON])
    @ResponseBody
    fun getDraftSubmissions(@AuthenticationPrincipal user: SecurityUser): List<String> =
        subDraftService.getSubmissionsDraft(user.id).map { it.data }

    @DeleteMapping("/{accNo}")
    fun deleteDraftSubmission(@AuthenticationPrincipal user: SecurityUser, @PathVariable accNo: String): Unit =
        subDraftService.deleteSubmissionDraft(user.id, accNo)

    @PutMapping("/{accNo}")
    @ResponseBody
    fun updateDraftSubmission(
        @AuthenticationPrincipal user: SecurityUser,
        @RequestBody content: String,
        @PathVariable accNo: String
    ): String =
        subDraftService.updateSubmissionDraft(user.id, accNo, content).data

    @PostMapping
    @ResponseBody
    fun createDraftSubmission(
        @AuthenticationPrincipal user: SecurityUser,
        @RequestBody content: String
    ): String =
        subDraftService.createSubmissionDraft(user.id, content).key
}
