package ac.uk.ebi.biostd.persistence.common.service

import ac.uk.ebi.biostd.persistence.common.model.BasicCollection
import ac.uk.ebi.biostd.persistence.common.model.BasicSubmission
import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestFile
import ac.uk.ebi.biostd.persistence.common.request.PageRequest
import ac.uk.ebi.biostd.persistence.common.request.SubmissionFilter
import ac.uk.ebi.biostd.persistence.common.request.SubmissionListFilter
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.ExtSubmissionInfo
import ebi.ac.uk.model.RequestStatus
import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.Page
import java.time.Instant
import java.time.temporal.TemporalAmount

interface SubmissionPersistenceService {
    suspend fun saveSubmission(submission: ExtSubmission): ExtSubmission

    suspend fun expirePreviousVersions(accNo: String)

    suspend fun expireSubmissions(accNumbers: List<String>)

    suspend fun expireSubmission(accNo: String) = expireSubmissions(listOf(accNo))

    suspend fun setAsReleased(accNo: String)

    suspend fun getNextVersion(accNo: String): Int
}

@Suppress("TooManyFunctions")
interface SubmissionPersistenceQueryService {
    suspend fun existByAccNo(accNo: String): Boolean

    suspend fun existByAccNoAndVersion(
        accNo: String,
        version: Int,
    ): Boolean

    suspend fun existActiveByAccNo(accNo: String): Boolean

    suspend fun findExtByAccNo(
        accNo: String,
        includeFileListFiles: Boolean = false,
    ): ExtSubmission?

    suspend fun findLatestInactiveByAccNo(
        accNo: String,
        includeFileListFiles: Boolean = false,
    ): ExtSubmission?

    suspend fun getExtByAccNo(
        accNo: String,
        includeFileListFiles: Boolean = false,
    ): ExtSubmission

    suspend fun getExtByAccNoAndVersion(
        accNo: String,
        version: Int,
        includeFileListFiles: Boolean = false,
    ): ExtSubmission

    suspend fun getExtendedSubmissions(filter: SubmissionFilter): Page<ExtSubmission>

    suspend fun findCoreInfo(accNo: String): ExtSubmissionInfo?

    suspend fun getCoreInfoByAccNoAndVersion(
        accNo: String,
        version: Int,
    ): ExtSubmissionInfo

    /**
     * Return the list of submissions that belongs to a user. Both processed and processing or requesting ones are
     * retrieved.
     *
     * @param filter the submission filter
     **/
    suspend fun getSubmissionsByUser(filter: SubmissionListFilter): List<BasicSubmission>
}

interface SubmissionFilesPersistenceService {
    fun getReferencedFiles(
        sub: ExtSubmission,
        fileListName: String,
    ): Flow<ExtFile>

    suspend fun findReferencedFile(
        sub: ExtSubmission,
        path: String,
    ): ExtFile?
}

@Suppress("TooManyFunctions")
interface SubmissionRequestPersistenceService {
    suspend fun findRequestDrafts(
        owner: String,
        pageRequest: PageRequest = PageRequest(),
    ): Flow<SubmissionRequest>

    suspend fun findRequestDraft(
        accNo: String,
        owner: String,
    ): SubmissionRequest?

    suspend fun findSubmissionRequestDraft(accNo: String): SubmissionRequest?

    suspend fun deleteRequestDraft(
        accNo: String,
        owner: String,
    )

    suspend fun updateRequestDraft(
        accNo: String,
        owner: String,
        draft: String,
        modificationTime: Instant,
    )

    suspend fun setSubRequestAccNo(
        tempAccNo: String,
        accNo: String,
        owner: String,
        modificationTime: Instant,
    )

    suspend fun setDraftStatus(
        accNo: String,
        owner: String,
        status: RequestStatus,
        modificationTime: Instant,
    )

    suspend fun findAllProcessed(): Flow<Pair<String, Int>>

    suspend fun hasActiveRequest(accNo: String): Boolean

    suspend fun saveRequest(rqt: SubmissionRequest): Pair<String, Int>

    fun getProcessingRequests(since: TemporalAmount? = null): Flow<Pair<String, Int>>

    /**
     * Update the given request file. By default, only file index is updated in submission request. For other options
     * @see UpdateOptions
     */
    suspend fun updateRqtFile(rqt: SubmissionRequestFile)

    suspend fun getRequest(
        accNo: String,
        version: Int,
    ): SubmissionRequest

    suspend fun onRequest(
        accNo: String,
        version: Int,
        status: RequestStatus,
        processId: String,
        handler: suspend (SubmissionRequest) -> SubmissionRequest,
    ): SubmissionRequest

    suspend fun isRequestCompleted(
        accNo: String,
        version: Int,
    ): Boolean

    suspend fun archiveRequest(
        accNo: String,
        version: Int,
    )
}

interface SubmissionRequestFilesPersistenceService {
    suspend fun saveSubmissionRequestFile(file: SubmissionRequestFile)

    suspend fun getSubmissionRequestFile(
        accNo: String,
        version: Int,
        filePath: String,
    ): SubmissionRequestFile

    fun getSubmissionRequestFiles(
        accNo: String,
        version: Int,
        startingAt: Int,
    ): Flow<SubmissionRequestFile>

    fun getSubmissionRequestFiles(
        accNo: String,
        version: Int,
        status: RequestFileStatus,
    ): Flow<SubmissionRequestFile>
}

interface SubmissionMetaQueryService {
    suspend fun getBasicCollection(accNo: String): BasicCollection

    suspend fun findLatestBasicByAccNo(accNo: String): BasicSubmission?

    suspend fun getCollections(accNo: String): List<String>

    suspend fun existByAccNo(accNo: String): Boolean
}
