package ac.uk.ebi.biostd.persistence.common.service

import ac.uk.ebi.biostd.persistence.common.model.BasicCollection
import ac.uk.ebi.biostd.persistence.common.model.BasicSubmission
import ac.uk.ebi.biostd.persistence.common.request.SaveSubmissionRequest
import ac.uk.ebi.biostd.persistence.common.request.SubmissionFilter
import ebi.ac.uk.extended.model.ExtSubmission
import org.springframework.data.domain.Page

interface SubmissionRequestService {
    fun saveAndProcessSubmissionRequest(saveRequest: SaveSubmissionRequest): ExtSubmission

    fun saveSubmissionRequest(saveRequest: SaveSubmissionRequest): ExtSubmission

    fun processSubmission(saveRequest: SaveSubmissionRequest): ExtSubmission

    fun refreshSubmission(submission: ExtSubmission)
}

interface SubmissionQueryService {
    fun existByAccNo(accNo: String): Boolean

    fun findExtByAccNo(accNo: String): ExtSubmission?

    fun getExtByAccNo(accNo: String): ExtSubmission

    fun getExtByAccNoAndVersion(accNo: String, version: Int): ExtSubmission

    fun expireSubmissions(accNumbers: List<String>)

    fun expireSubmission(accNo: String) = expireSubmissions(listOf(accNo))

    fun getExtendedSubmissions(filter: SubmissionFilter): Page<Result<ExtSubmission>>

    /**
     * Return the list of submissions that belongs to a user. Both processed and processing or requesting ones are
     * retrieved.
     *
     * @param email the submission owner email
     * @param filter the submission filter
     **/
    fun getSubmissionsByUser(email: String, filter: SubmissionFilter): List<BasicSubmission>

    fun getRequest(accNo: String, version: Int): ExtSubmission
}

interface SubmissionMetaQueryService {
    fun getBasicCollection(accNo: String): BasicCollection

    fun findLatestBasicByAccNo(accNo: String): BasicSubmission?

    fun getAccessTags(accNo: String): List<String>

    fun existByAccNo(accNo: String): Boolean
}
