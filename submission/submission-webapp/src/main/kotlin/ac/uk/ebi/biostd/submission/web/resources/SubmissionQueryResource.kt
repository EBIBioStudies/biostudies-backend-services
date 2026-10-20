package ac.uk.ebi.biostd.submission.web.resources

import ac.uk.ebi.biostd.persistence.common.model.BasicSubmission
import ac.uk.ebi.biostd.submission.converters.BioUser
import ac.uk.ebi.biostd.submission.web.handlers.SubmissionsWebHandler
import ac.uk.ebi.biostd.submission.web.model.SubmissionFilterRequest
import ac.uk.ebi.biostd.submission.web.model.asFilter
import ebi.ac.uk.api.dto.SubmissionDto
import ebi.ac.uk.security.integration.model.api.SecurityUser
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/submissions")
class SubmissionQueryResource(
    private val submissionsWebHandler: SubmissionsWebHandler,
) {
    @GetMapping
    suspend fun getSubmissions(
        @BioUser user: SecurityUser,
        @ModelAttribute request: SubmissionFilterRequest,
    ): List<SubmissionDto> {
        val filter = request.asFilter(user.email, user.superuser, user.adminCollections)
        return submissionsWebHandler.getSubmissions(filter).map { it.asDto() }
    }

    private fun BasicSubmission.asDto() =
        SubmissionDto(
            accno = accNo,
            displayAccNo = displayAccNo,
            newSubmission = newSubmission,
            title = title.orEmpty(),
            mtime = modificationTime,
            rtime = releaseTime,
            status = status.value,
            errors = errors,
        )
}
