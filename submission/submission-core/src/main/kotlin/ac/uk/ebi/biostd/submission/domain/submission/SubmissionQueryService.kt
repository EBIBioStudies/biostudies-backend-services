package ac.uk.ebi.biostd.submission.domain.submission

import ac.uk.ebi.biostd.persistence.common.model.BasicSubmission
import ac.uk.ebi.biostd.persistence.common.request.SubmissionListFilter
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService

class SubmissionQueryService(
    private val submissionPersistenceQueryService: SubmissionPersistenceQueryService,
) {
    suspend fun getSubmissions(filter: SubmissionListFilter): List<BasicSubmission> =
        submissionPersistenceQueryService.getSubmissionsByUser(filter)
}
