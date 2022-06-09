package ac.uk.ebi.biostd.submission.web.handlers

import ac.uk.ebi.biostd.persistence.common.model.BasicSubmission
import ac.uk.ebi.biostd.persistence.common.request.SubmissionFilter
import ac.uk.ebi.biostd.submission.domain.service.SubmissionQueryService
import ac.uk.ebi.biostd.submission.domain.service.SubmissionService
import ac.uk.ebi.biostd.submission.model.ReleaseRequest
import ebi.ac.uk.security.integration.model.api.SecurityUser

class SubmissionsWebHandler(
    private val submissionService: SubmissionService,
    private val submissionQueryService: SubmissionQueryService
) {
    fun deleteSubmission(accNo: String, user: SecurityUser): Unit = submissionService.deleteSubmission(accNo, user)

    fun deleteSubmissions(submissions: List<String>, user: SecurityUser): Unit =
        submissionService.deleteSubmissions(submissions, user)

    fun releaseSubmission(request: ReleaseRequest, user: SecurityUser): Unit =
        submissionService.releaseSubmission(request, user)

    fun getSubmissions(user: SecurityUser, filter: SubmissionFilter): List<BasicSubmission> =
        submissionQueryService.getSubmissions(user, filter)
}
