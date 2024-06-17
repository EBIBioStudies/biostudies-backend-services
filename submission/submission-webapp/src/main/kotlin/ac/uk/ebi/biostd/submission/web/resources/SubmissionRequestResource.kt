package ac.uk.ebi.biostd.submission.web.resources

import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.submission.domain.service.SubmissionRequestService
import ebi.ac.uk.model.RequestStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/submissions/requests")
@PreAuthorize("isAuthenticated()")
class SubmissionRequestResource(
    private val submissionRequestService: SubmissionRequestService,
) {
    @GetMapping("/{accNo}/{version}")
    @ResponseBody
    suspend fun getSubmissionRequest(
        @PathVariable accNo: String,
        @PathVariable version: Int,
    ): SubmissionRequest {
        return submissionRequestService.getSubmissionRequest(accNo, version)
    }

    @GetMapping("/{accNo}/{version}/status")
    @ResponseBody
    suspend fun getSubmissionRequestStatus(
        @PathVariable accNo: String,
        @PathVariable version: Int,
    ): RequestStatus {
        return getSubmissionRequest(accNo, version).status
    }
}
