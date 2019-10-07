package ac.uk.ebi.biostd.data.web

import ac.uk.ebi.biostd.data.service.DraftSubService
import ac.uk.ebi.biostd.persistence.model.UserData
import ebi.ac.uk.model.constants.APPLICATION_JSON
import ebi.ac.uk.security.integration.model.api.SecurityUser
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("submissions/drafts")
@PreAuthorize("isAuthenticated()")
class DraftSubmissionResource(private val draftSubService: DraftSubService) {
    @GetMapping(value = ["/{accNo}"], produces = [APPLICATION_JSON])
    @ResponseBody
    fun getDraftSubmission(@AuthenticationPrincipal user: SecurityUser, @PathVariable accNo: String): UserData =
        draftSubService.getSubmission(user.id, accNo)

    @GetMapping(params = ["searchText"], produces = [APPLICATION_JSON])
    @ResponseBody
    fun searchTmpSubmission(@AuthenticationPrincipal user: SecurityUser, @RequestParam searchText: String):
        List<UserData> = draftSubService.searchDraftSubmissions(user.id, searchText)

    @GetMapping(params = ["!searchText"])
    @ResponseBody
    fun getDraftSubmissions(@AuthenticationPrincipal user: SecurityUser): List<UserData> =
        draftSubService.getDraftSubmissions(user.id)

    @DeleteMapping("/{accNo}")
    fun deleteDraftSubmission(@AuthenticationPrincipal user: SecurityUser, @PathVariable accNo: String): Unit =
        draftSubService.deleteDraftSubmission(user.id, accNo)

    @PostMapping()
    @ResponseBody
    fun deleteDraftSubmission(@AuthenticationPrincipal user: SecurityUser, @RequestBody userData: UserData): UserData =
        draftSubService.updateDraftSubmission(user.id, userData.key, userData.data)
}
