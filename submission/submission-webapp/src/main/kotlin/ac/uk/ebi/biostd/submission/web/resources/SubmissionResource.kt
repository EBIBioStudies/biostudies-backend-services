package ac.uk.ebi.biostd.submission.web.resources

import ac.uk.ebi.biostd.persistence.common.model.BasicSubmission
import ac.uk.ebi.biostd.submission.converters.BioUser
import ac.uk.ebi.biostd.submission.model.ReleaseRequest
import ac.uk.ebi.biostd.submission.web.handlers.SubmissionsWebHandler
import ac.uk.ebi.biostd.submission.web.model.SubmissionFilterRequest
import ac.uk.ebi.biostd.submission.web.model.asFilter
import ebi.ac.uk.api.dto.SubmissionDto
import ebi.ac.uk.security.integration.model.api.SecurityUser
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestParam

@RestController
@RequestMapping("/submissions")
@PreAuthorize("isAuthenticated()")
class SubmissionResource(
    private val submissionsWebHandler: SubmissionsWebHandler
) {
    @GetMapping
    fun getSubmissions(
        @BioUser user: SecurityUser,
        @ModelAttribute request: SubmissionFilterRequest
    ): List<SubmissionDto> = submissionsWebHandler.getSubmissions(user, request.asFilter()).map { it.asDto() }

    @DeleteMapping("/{accNo}")
    fun deleteSubmission(
        @BioUser user: SecurityUser,
        @PathVariable accNo: String
    ): Unit = submissionsWebHandler.deleteSubmission(accNo, user)

    @DeleteMapping
    fun deleteSubmissions(
        @BioUser user: SecurityUser,
        @RequestParam submissions: List<String>
    ): Unit = submissionsWebHandler.deleteSubmissions(submissions, user)

    @PutMapping("/release")
    fun releaseSubmission(
        @BioUser user: SecurityUser,
        @RequestBody request: ReleaseRequest
    ): Unit = submissionsWebHandler.releaseSubmission(request, user)

    private fun BasicSubmission.asDto() =
        SubmissionDto(
            accNo,
            title.orEmpty(),
            version,
            creationTime,
            modificationTime,
            releaseTime,
            method,
            status.value
        )
}
