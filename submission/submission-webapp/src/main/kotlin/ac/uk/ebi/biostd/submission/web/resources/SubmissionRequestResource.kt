package ac.uk.ebi.biostd.submission.web.resources

import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ebi.ac.uk.model.RequestStatus
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
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
@Tag(name = "Submission Requests", description = "Track asynchronous submission processing requests and their validation results.")
class SubmissionRequestResource(
    private val submissionRequestService: SubmissionRequestPersistenceService,
) {
    @GetMapping("/{accNo}/{version}")
    @ResponseBody
    @Operation(
        summary = "Get Submission Request",
        description = "Return the stored processing request for a submission accession and version.",
    )
    suspend fun getSubmissionRequest(
        @PathVariable accNo: String,
        @PathVariable version: Int,
    ): SubmissionRequest = submissionRequestService.getRequest(accNo, version)

    @GetMapping("/{accNo}/{version}/status")
    @ResponseBody
    @Operation(
        summary = "Get Request Status",
        description = "Return the current processing status for a submission request.",
    )
    suspend fun getSubmissionRequestStatus(
        @PathVariable accNo: String,
        @PathVariable version: Int,
    ): RequestStatus = submissionRequestService.getRequest(accNo, version).status

    @GetMapping("/{accNo}/{version}/errors")
    @ResponseBody
    @Operation(
        summary = "Get Request Errors",
        description = "Return validation or processing errors recorded for a submission request.",
    )
    suspend fun getSubmissionRequestErrors(
        @PathVariable accNo: String,
        @PathVariable version: Int,
    ): List<String> = submissionRequestService.getRequest(accNo, version).errors

    @PostMapping("/{accNo}/{version}/archive")
    @ResponseBody
    @Operation(
        summary = "Archive Submission Request",
        description = "Archive an old processing request once it no longer needs to appear in active request views.",
    )
    suspend fun archiveSubmissionRequest(
        @PathVariable accNo: String,
        @PathVariable version: Int,
    ) {
        submissionRequestService.archiveRequest(accNo, version)
    }
}
