package ac.uk.ebi.biostd.persistence.common.service

import ac.uk.ebi.biostd.persistence.common.model.BasicCollection
import ac.uk.ebi.biostd.persistence.common.model.BasicSubmission
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestFile
import ac.uk.ebi.biostd.persistence.common.request.SubmissionFilter
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import org.springframework.data.domain.Page

interface SubmissionPersistenceService {
    fun saveSubmission(submission: ExtSubmission): ExtSubmission

    fun expirePreviousVersions(accNo: String)

    fun expireSubmissions(accNumbers: List<String>)

    fun expireSubmission(accNo: String) = expireSubmissions(listOf(accNo))

    fun setAsReleased(accNo: String)

    fun getNextVersion(accNo: String): Int
}

interface SubmissionPersistenceQueryService {
    fun existByAccNo(accNo: String): Boolean

    fun existByAccNoAndVersion(accNo: String, version: Int): Boolean

    fun findExtByAccNo(accNo: String, includeFileListFiles: Boolean = false): ExtSubmission?

    fun findLatestExtByAccNo(accNo: String, includeFileListFiles: Boolean = false): ExtSubmission?

    fun getExtByAccNo(accNo: String, includeFileListFiles: Boolean = false): ExtSubmission

    fun getExtByAccNoAndVersion(accNo: String, version: Int, includeFileListFiles: Boolean = false): ExtSubmission

    fun getExtendedSubmissions(filter: SubmissionFilter): Page<ExtSubmission>

    /**
     * Return the list of submissions that belongs to a user. Both processed and processing or requesting ones are
     * retrieved.
     *
     * @param owner the submission owner email
     * @param filter the submission filter
     **/
    fun getSubmissionsByUser(owner: String, filter: SubmissionFilter): List<BasicSubmission>

    fun getReferencedFiles(accNo: String, fileListName: String): List<ExtFile>

    fun findReferencedFile(accNo: String, version: Int, path: String): ExtFile?
}

@Suppress("TooManyFunctions")
interface SubmissionRequestPersistenceService {
    fun hasActiveRequest(accNo: String): Boolean

    fun saveSubmissionRequest(rqt: SubmissionRequest): Pair<String, Int>

    fun createSubmissionRequest(rqt: SubmissionRequest): Pair<String, Int>

    fun updateRequestStatus(accNo: String, version: Int, status: RequestStatus)

    fun updateRequestFile(file: SubmissionRequestFile)

    fun updateRequestTotalFiles(accNo: String, version: Int, totalFiles: Int)

    fun getPendingRequest(accNo: String, version: Int): SubmissionRequest

    fun getIndexedRequest(accNo: String, version: Int): SubmissionRequest

    fun getLoadedRequest(accNo: String, version: Int): SubmissionRequest

    fun getCleanedRequest(accNo: String, version: Int): SubmissionRequest

    fun getFilesCopiedRequest(accNo: String, version: Int): SubmissionRequest

    fun getRequestStatus(accNo: String, version: Int): RequestStatus
}

interface SubmissionRequestFilesPersistenceService {
    fun saveSubmissionRequestFile(file: SubmissionRequestFile)

    fun getSubmissionRequestFile(accNo: String, version: Int, filePath: String): SubmissionRequestFile

    fun getSubmissionRequestFiles(accNo: String, version: Int, startingAt: Int): Sequence<SubmissionRequestFile>
}

interface SubmissionMetaQueryService {
    fun getBasicCollection(accNo: String): BasicCollection

    fun findLatestBasicByAccNo(accNo: String): BasicSubmission?

    fun getAccessTags(accNo: String): List<String>

    fun existByAccNo(accNo: String): Boolean
}
