package ac.uk.ebi.biostd.persistence.common.service

import ac.uk.ebi.biostd.persistence.common.model.SimpleSubmission
import ac.uk.ebi.biostd.persistence.common.request.SaveSubmissionRequest
import ac.uk.ebi.biostd.persistence.common.request.SubmissionFilter
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FileMode
import org.springframework.data.domain.Page

interface SubmissionRequestService {
    fun saveAndProcessSubmissionRequest(saveRequest: SaveSubmissionRequest): ExtSubmission
    fun saveSubmissionRequest(saveRequest: SaveSubmissionRequest): ExtSubmission
    fun processSubmission(saveRequest: SaveSubmissionRequest): ExtSubmission
    fun refreshSubmission(submission: ExtSubmission)
}

interface SubmissionPersistenceService {
    fun saveSubmissionRequest(submission: ExtSubmission): ExtSubmission
    fun processSubmission(submission: ExtSubmission, mode: FileMode): ExtSubmission
}

interface SubmissionQueryService {
    fun existByAccNo(accNo: String): Boolean
    fun getExtByAccNo(accNo: String): ExtSubmission
    fun getExtByAccNoAndVersion(accNo: String, version: Int): ExtSubmission
    fun expireSubmission(accNo: String)
    fun getExtendedSubmissions(offset: Long, limit: Int): Page<ExtSubmission>
    fun getSubmissionsByUser(userId: Long, filter: SubmissionFilter): List<SimpleSubmission>
}
