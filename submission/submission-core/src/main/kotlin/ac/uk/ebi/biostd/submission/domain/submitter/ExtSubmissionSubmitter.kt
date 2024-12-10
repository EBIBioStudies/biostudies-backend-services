package ac.uk.ebi.biostd.submission.domain.submitter

import ac.uk.ebi.biostd.persistence.common.request.ExtSubmitRequest
import ebi.ac.uk.extended.model.ExtSubmission

@Suppress("TooManyFunctions")
interface ExtSubmissionSubmitter {
    suspend fun createRqt(rqt: ExtSubmitRequest): Pair<String, Int>

//    suspend fun processRequestDraft(rqt: ExtSubmitRequest): Pair<String, Int>

    suspend fun handleRequest(
        accNo: String,
        version: Int,
    ): ExtSubmission

    suspend fun handleRequestAsync(
        accNo: String,
        version: Int,
    )
}
