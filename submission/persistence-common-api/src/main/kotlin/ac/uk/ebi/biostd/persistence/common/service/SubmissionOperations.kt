package ac.uk.ebi.biostd.persistence.common.service

import ac.uk.ebi.biostd.persistence.common.model.BasicProject
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

    fun getExtByAccNo(accNo: String): ExtSubmission

    fun getExtByAccNoAndVersion(accNo: String, version: Int): ExtSubmission

    fun expireSubmission(accNo: String)

    fun getExtendedSubmissions(filter: SubmissionFilter, offset: Long, limit: Int): Page<ExtSubmission>

    fun getSubmissionsByUser(userId: Long, filter: SubmissionFilter): List<BasicSubmission>

    fun getRequest(accNo: String, version: Int): ExtSubmission
}

interface SubmissionMetaQueryService {
    fun getBasicProject(accNo: String): BasicProject

    fun findLatestBasicByAccNo(accNo: String): BasicSubmission?

    fun getAccessTags(accNo: String): List<String>

    fun existByAccNo(accNo: String): Boolean
}
