package ac.uk.ebi.biostd.submission.web.resources

import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ebi.ac.uk.model.RequestStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/submissions/requests")
@PreAuthorize("isAuthenticated()")
class SubmissionRequestResource(
    private val submissionRequestService: SubmissionRequestPersistenceService,
) {
    @GetMapping("/{accNo}/{version}")
    @ResponseBody
    suspend fun getSubmissionRequest(
        @PathVariable accNo: String,
        @PathVariable version: Int,
    ): SubmissionRequest = submissionRequestService.getRequest(accNo, version)

    @GetMapping("/{accNo}/{version}/status")
    @ResponseBody
    suspend fun getSubmissionRequestStatus(
        @PathVariable accNo: String,
        @PathVariable version: Int,
    ): RequestStatus = submissionRequestService.getRequest(accNo, version).status

    @PostMapping("/{accNo}/{version}/archive")
    @ResponseBody
    suspend fun archiveSubmissionRequest(
        @PathVariable accNo: String,
        @PathVariable version: Int,
    ) {
        submissionRequestService.archiveRequest(accNo, version)
    }
}
