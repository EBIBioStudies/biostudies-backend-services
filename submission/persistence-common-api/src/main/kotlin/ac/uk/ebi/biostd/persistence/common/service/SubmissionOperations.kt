package ac.uk.ebi.biostd.persistence.common.service

import ac.uk.ebi.biostd.persistence.common.model.BasicCollection
import ac.uk.ebi.biostd.persistence.common.model.BasicSubmission
import ac.uk.ebi.biostd.persistence.common.request.SubmissionFilter
import ac.uk.ebi.biostd.persistence.common.request.SubmissionRequest
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import org.springframework.data.domain.Page

interface SubmissionRequestService {
    fun saveSubmissionRequest(rqt: SubmissionRequest): Pair<String, Int>

    fun processSubmissionRequest(saveRequest: SubmissionRequest): ExtSubmission
}

interface SubmissionQueryService {
    fun existByAccNo(accNo: String): Boolean

    fun findExtByAccNo(accNo: String, includeFileListFiles: Boolean = false): ExtSubmission?

    fun getExtByAccNo(accNo: String, includeFileListFiles: Boolean = false): ExtSubmission

    fun getExtByAccNoAndVersion(accNo: String, version: Int, includeFileListFiles: Boolean = false): ExtSubmission

    fun expireSubmissions(accNumbers: List<String>)

    fun expireSubmission(accNo: String) = expireSubmissions(listOf(accNo))

    fun getExtendedSubmissions(filter: SubmissionFilter): Page<ExtSubmission>

    /**
     * Return the list of submissions that belongs to a user. Both processed and processing or requesting ones are
     * retrieved.
     *
     * @param owner the submission owner email
     * @param filter the submission filter
     **/
    fun getSubmissionsByUser(owner: String, filter: SubmissionFilter): List<BasicSubmission>

    fun getPendingRequest(accNo: String, version: Int): SubmissionRequest

    fun getReferencedFiles(accNo: String, fileListName: String): List<ExtFile>
}

interface SubmissionMetaQueryService {
    fun getBasicCollection(accNo: String): BasicCollection

    fun findLatestBasicByAccNo(accNo: String): BasicSubmission?

    fun getAccessTags(accNo: String): List<String>

    fun existByAccNo(accNo: String): Boolean
}
