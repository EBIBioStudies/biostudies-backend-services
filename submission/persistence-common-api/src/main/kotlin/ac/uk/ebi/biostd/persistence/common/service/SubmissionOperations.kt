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
import ebi.ac.uk.extended.model.ExtLink
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.ExtSubmissionInfo
import ebi.ac.uk.model.RequestStatus
import ebi.ac.uk.model.SubmissionId
import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.Page
import org.springframework.data.mongodb.core.query.Meta.CursorOption
import org.springframework.data.mongodb.repository.Meta
import java.time.Instant
import java.time.temporal.TemporalAmount

interface SubmissionPersistenceService {
    suspend fun saveSubmission(submission: ExtSubmission): ExtSubmission

    suspend fun expirePreviousVersions(accNo: String)

    suspend fun expireSubmissions(accNumbers: List<String>)

    suspend fun expireSubmission(accNo: String) = expireSubmissions(listOf(accNo))

    suspend fun getNextVersion(accNo: String): Int

    suspend fun setOwner(
        accNo: String,
        owner: String,
    )
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
        includeLinkListLinks: Boolean = false,
    ): ExtSubmission?

    suspend fun findLatestInactiveByAccNo(
        accNo: String,
        includeFileListFiles: Boolean = false,
        includeLinkListLinks: Boolean = false,
    ): ExtSubmission?

    suspend fun getExtByAccNo(
        accNo: String,
        includeFileListFiles: Boolean = false,
        includeLinkListLinks: Boolean = false,
    ): ExtSubmission

    suspend fun getExtByAccNoAndVersion(
        accNo: String,
        version: Int,
        includeFileListFiles: Boolean = false,
        includeLinkListLinks: Boolean = false,
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

    suspend fun getSubmissionsByOwner(
        owner: String,
        accNoList: List<String> = emptyList(),
    ): Flow<BasicSubmission>

    @Meta(flags = [CursorOption.NO_TIMEOUT])
    suspend fun findAllActive(
        includeFileListFiles: Boolean,
        includeLinkListLinks: Boolean,
    ): Flow<ExtSubmission>
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

interface SubmissionLinksPersistenceService {
    fun getReferencedLinks(
        accNo: String,
        linkListName: String,
    ): Flow<ExtLink>
}

@Suppress("TooManyFunctions")
interface SubmissionRequestPersistenceService {
    suspend fun findRequestDrafts(
        owner: String,
        pageRequest: PageRequest = PageRequest(),
    ): Flow<SubmissionRequest>

    suspend fun findEditableRequest(
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

    suspend fun setSubRequestErrors(
        accNo: String,
        owner: String,
        errors: List<String>,
        modificationTime: Instant,
    )

    suspend fun setDraftStatus(
        accNo: String,
        owner: String,
        status: RequestStatus,
        modificationTime: Instant,
    )

    suspend fun findAllCompleted(): Flow<SubmissionId>

    suspend fun hasProcessingRequest(accNo: String): Boolean

    suspend fun saveRequest(rqt: SubmissionRequest): SubmissionId

    fun getActiveRequests(since: TemporalAmount? = null): Flow<SubmissionId>

    /**
     * Updates the given request files. The submission request index is updated based on the given number of elements.
     */
    suspend fun updateRqtFiles(rqtFiles: List<SubmissionRequestFile>)

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
