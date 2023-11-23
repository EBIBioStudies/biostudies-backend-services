package ac.uk.ebi.biostd.submission.domain.submitter

import ac.uk.ebi.biostd.persistence.common.request.ExtSubmitRequest
import ebi.ac.uk.extended.model.ExtSubmission

interface ExtSubmissionSubmitter {
    suspend fun createRequest(rqt: ExtSubmitRequest): Pair<String, Int>
    suspend fun indexRequest(accNo: String, version: Int): Unit
    suspend fun loadRequest(accNo: String, version: Int): Unit
    suspend fun cleanRequest(accNo: String, version: Int): Unit
    suspend fun processRequest(accNo: String, version: Int): Unit
    suspend fun checkReleased(accNo: String, version: Int): Unit
    suspend fun saveRequest(accNo: String, version: Int): ExtSubmission
    suspend fun finalizeRequest(accNo: String, version: Int): ExtSubmission
    suspend fun release(accNo: String)
    suspend fun handleRequest(accNo: String, version: Int): ExtSubmission
}
