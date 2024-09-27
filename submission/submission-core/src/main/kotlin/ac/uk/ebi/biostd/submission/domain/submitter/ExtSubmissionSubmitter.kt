package ac.uk.ebi.biostd.submission.domain.submitter

import ac.uk.ebi.biostd.persistence.common.request.ExtSubmitRequest
import ebi.ac.uk.extended.model.ExtSubmission

@Suppress("TooManyFunctions")
interface ExtSubmissionSubmitter {
    suspend fun createRqt(rqt: ExtSubmitRequest): Pair<String, Int>

    suspend fun indexRequest(
        accNo: String,
        version: Int,
    )

    suspend fun loadRequest(
        accNo: String,
        version: Int,
    )

    suspend fun indexToCleanRequest(
        accNo: String,
        version: Int,
    )

    suspend fun validateRequest(
        accNo: String,
        version: Int,
    )

    suspend fun cleanRequest(
        accNo: String,
        version: Int,
    )

    suspend fun processRequest(
        accNo: String,
        version: Int,
    )

    suspend fun checkReleased(
        accNo: String,
        version: Int,
    )

    suspend fun saveRequest(
        accNo: String,
        version: Int,
    )

    suspend fun finalizeRequest(
        accNo: String,
        version: Int,
    )

    suspend fun handleRequest(
        accNo: String,
        version: Int,
    ): ExtSubmission

    suspend fun completeRqt(
        accNo: String,
        version: Int,
    )
}
