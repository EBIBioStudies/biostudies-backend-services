package ac.uk.ebi.biostd.submission.web.handlers

import ac.uk.ebi.biostd.persistence.common.model.BasicSubmission
import ac.uk.ebi.biostd.persistence.common.request.ListFilter
import ac.uk.ebi.biostd.submission.domain.submission.SubmissionQueryService
import ac.uk.ebi.biostd.submission.domain.submission.SubmissionService
import ebi.ac.uk.security.integration.model.api.SecurityUser

class SubmissionsWebHandler(
    private val submissionService: SubmissionService,
    private val submissionQueryService: SubmissionQueryService,
) {
    suspend fun deleteSubmission(
        accNo: String,
        user: SecurityUser,
    ): Unit = submissionService.deleteSubmission(accNo, user)

    suspend fun deleteSubmissions(
        submissions: List<String>,
        user: SecurityUser,
    ): Unit = submissionService.deleteSubmissions(submissions, user)

    suspend fun getSubmissions(filter: ListFilter): List<BasicSubmission> = submissionQueryService.getSubmissions(filter)
}
