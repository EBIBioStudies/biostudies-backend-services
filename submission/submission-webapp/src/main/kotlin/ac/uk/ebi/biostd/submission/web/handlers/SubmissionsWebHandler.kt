package ac.uk.ebi.biostd.submission.web.handlers

import ac.uk.ebi.biostd.persistence.filter.SubmissionFilter
import ac.uk.ebi.biostd.submission.domain.service.SubmissionService
import ebi.ac.uk.security.integration.model.api.SecurityUser

class SubmissionsWebHandler(private val submissionService: SubmissionService) {

    fun deleteSubmission(accNo: String, user: SecurityUser): Unit = submissionService.deleteSubmission(accNo, user)

    fun getSubmissions(user: SecurityUser, filter: SubmissionFilter) = submissionService.getSubmissions(user, filter)
}
