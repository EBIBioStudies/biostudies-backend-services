package ac.uk.ebi.biostd.data.web

import ac.uk.ebi.biostd.data.service.SubDraftService
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
class SubmissionDraftResource(private val subDraftService: SubDraftService) {
    @GetMapping(value = ["/{accNo}"], produces = [APPLICATION_JSON])
    @ResponseBody
    fun getDraftSubmission(@AuthenticationPrincipal user: SecurityUser, @PathVariable accNo: String): UserData =
        subDraftService.getSubmissionDraft(user.id, accNo)

    @GetMapping(params = ["searchText"], produces = [APPLICATION_JSON])
    @ResponseBody
    fun searchTmpSubmission(@AuthenticationPrincipal user: SecurityUser, @RequestParam searchText: String):
        List<UserData> = subDraftService.searchSubmissionsDraft(user.id, searchText)

    @GetMapping(params = ["!searchText"])
    @ResponseBody
    fun getDraftSubmissions(@AuthenticationPrincipal user: SecurityUser): List<UserData> =
        subDraftService.getSubmissionsDraft(user.id)

    @DeleteMapping("/{accNo}")
    fun deleteDraftSubmission(@AuthenticationPrincipal user: SecurityUser, @PathVariable accNo: String): Unit =
        subDraftService.deleteSubmissionDraft(user.id, accNo)

    @PutMapping("/{accNo}")
    @ResponseBody
    fun updateDraftSubmission(
        @AuthenticationPrincipal user: SecurityUser,
        @RequestBody userData: UserData,
        @PathVariable accNo: String
    ): UserData =
        subDraftService.updateSubmissionDraft(user.id, accNo, userData.data)

    @PostMapping
    @ResponseBody
    fun createDraftSubmission(
        @AuthenticationPrincipal user: SecurityUser,
        @RequestBody content: DraftContent
    ): UserData =
        subDraftService.createSubmissionDraft(user.id, content.data)
}
