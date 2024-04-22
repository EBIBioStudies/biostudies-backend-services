package ac.uk.ebi.biostd.persistence.common.service

import ac.uk.ebi.biostd.persistence.common.model.BasicCollection
import ac.uk.ebi.biostd.persistence.common.model.BasicSubmission
import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestFile
import ac.uk.ebi.biostd.persistence.common.request.SubmissionFilter
import ac.uk.ebi.biostd.persistence.common.request.SubmissionListFilter
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.Page
import java.time.temporal.TemporalAmount

interface SubmissionPersistenceService {
    suspend fun saveSubmission(submission: ExtSubmission): ExtSubmission

    suspend fun expirePreviousVersions(accNo: String)

    suspend fun expireSubmissions(accNumbers: List<String>)

    suspend fun expireSubmission(accNo: String) = expireSubmissions(listOf(accNo))

    suspend fun setAsReleased(accNo: String)

    suspend fun getNextVersion(accNo: String): Int
}

interface SubmissionPersistenceQueryService {
    suspend fun existByAccNo(accNo: String): Boolean

    suspend fun existByAccNoAndVersion(
        accNo: String,
        version: Int,
    ): Boolean

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
    suspend fun hasActiveRequest(accNo: String): Boolean

    suspend fun createRequest(rqt: SubmissionRequest): Pair<String, Int>

    suspend fun getRequestStatus(
        accNo: String,
        version: Int,
    ): RequestStatus

    fun getProcessingRequests(since: TemporalAmount? = null): Flow<Pair<String, Int>>

    /**
     * Update the given request file. By default only file index is updated in submission request. For other options
     * @see UpdateOptions
     */
    suspend fun updateRqtFile(rqt: SubmissionRequestFile)

    suspend fun getSubmissionRequest(
        accNo: String,
        version: Int,
    ): SubmissionRequest

    suspend fun <T> onRequest(
        accNo: String,
        version: Int,
        status: RequestStatus,
        processId: String,
        handler: suspend (SubmissionRequest) -> OptResponse<T>,
    ): OptResponse<T>
}

sealed interface OptResponse<T> {
    val rqt: SubmissionRequest
    val value: T

    operator fun component1(): SubmissionRequest = rqt

    operator fun component2(): T = value
}

class RqtUpdate(override val rqt: SubmissionRequest, override val value: Unit = Unit) : OptResponse<Unit>

class RqtResponse(
    override val rqt: SubmissionRequest,
    override val value: ExtSubmission,
) : OptResponse<ExtSubmission>

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

    suspend fun getAccessTags(accNo: String): List<String>

    suspend fun existByAccNo(accNo: String): Boolean
}
