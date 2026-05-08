package ac.uk.ebi.biostd.submission.domain.submitter

import ac.uk.ebi.biostd.persistence.common.request.ExtSubmitRequest
import ac.uk.ebi.biostd.submission.domain.submission.SubmissionService.Companion.SYNC_SUBMIT_TIMEOUT
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.model.SubmissionId
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

@Suppress("TooManyFunctions")
interface ExtSubmissionSubmitter {
    suspend fun createRqt(rqt: ExtSubmitRequest): SubmissionId

    suspend fun handleRequest(
        accNo: String,
        version: Int,
        waitTime: Duration = SYNC_SUBMIT_TIMEOUT.minutes,
    ): ExtSubmission

    suspend fun handleMany(
        submissions: List<SubmissionId>,
        waitTime: Duration = SYNC_SUBMIT_TIMEOUT.minutes,
    ): List<ExtSubmission>

    suspend fun handleRequestAsync(
        accNo: String,
        version: Int,
    )

    suspend fun handleManyAsync(submissions: List<SubmissionId>)
}
