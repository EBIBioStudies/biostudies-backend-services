package ac.uk.ebi.biostd.submission.web.handlers

import ac.uk.ebi.biostd.submission.domain.service.ExtSubmissionService
import ac.uk.ebi.biostd.submission.web.model.RefreshWebRequest
import ebi.ac.uk.extended.mapping.to.toSimpleSubmission
import ebi.ac.uk.model.Submission

class ExtWebHandler(private val extSubmissionService: ExtSubmissionService) {

    fun refreshSubmission(request: RefreshWebRequest): Submission {
        val submission = extSubmissionService.refreshSubmission(request.accNo, request.user.email)
        return submission.toSimpleSubmission()
    }
}
