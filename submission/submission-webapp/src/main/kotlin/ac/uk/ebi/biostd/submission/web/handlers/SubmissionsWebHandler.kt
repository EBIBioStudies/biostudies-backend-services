package ac.uk.ebi.biostd.submission.web.handlers

import ac.uk.ebi.biostd.persistence.common.request.SubmissionFilter
import ac.uk.ebi.biostd.submission.domain.service.SubmissionService
import ac.uk.ebi.biostd.submission.model.ReleaseRequest
import ebi.ac.uk.security.integration.model.api.SecurityUser

class SubmissionsWebHandler(
    private val submissionService: SubmissionService
) {
    fun deleteSubmission(accNo: String, user: SecurityUser) {
        submissionService.deleteSubmission(accNo, user)
    }

    fun deleteSubmissions(
        submissions: List<String>,
        user: SecurityUser
    ) = submissionService.deleteSubmissions(submissions, user)

    fun releaseSubmission(request: ReleaseRequest, user: SecurityUser) {
        submissionService.releaseSubmission(request, user)
    }

    fun getSubmissions(user: SecurityUser, filter: SubmissionFilter) = submissionService.getSubmissions(user, filter)
}
