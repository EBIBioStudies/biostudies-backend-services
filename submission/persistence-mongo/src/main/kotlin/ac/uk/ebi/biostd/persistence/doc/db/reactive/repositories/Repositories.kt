package ac.uk.ebi.biostd.persistence.doc.db.reactive.repositories

import ac.uk.ebi.biostd.persistence.common.exception.SubmissionNotFoundException
import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType
import ac.uk.ebi.biostd.persistence.doc.db.repositories.SubmissionCollections
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequest
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequestFile
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionStats
import ac.uk.ebi.biostd.persistence.doc.model.FileListDocFile
import kotlinx.coroutines.flow.Flow
import org.bson.types.ObjectId
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.query.Meta.CursorOption
import org.springframework.data.mongodb.repository.Meta
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.time.Instant

interface SubmissionDraftRepository : CoroutineCrudRepository<DocSubmissionDraft, String> {
    suspend fun findByUserIdAndKeyAndStatusIsNot(
        userId: String,
        key: String,
        deleted: DocSubmissionDraft.DraftStatus,
    ): DocSubmissionDraft?

    fun findAllByUserIdAndStatus(
        userId: String,
        status: DocSubmissionDraft.DraftStatus,
        pageRequest: Pageable,
    ): Flow<DocSubmissionDraft>

    suspend fun getById(id: String): DocSubmissionDraft

    suspend fun deleteByUserIdAndKey(
        userId: String,
        draftKey: String,
    )
}

interface SubmissionStatsRepository : CoroutineCrudRepository<DocSubmissionStats, ObjectId> {
    suspend fun getByAccNo(accNo: String): DocSubmissionStats

    suspend fun findByAccNo(accNo: String): DocSubmissionStats?

    @Query("{ 'accNo': '?0', 'stats.?1': { \$exists: true } }")
    suspend fun findByAccNoAndStatType(
        accNo: String,
        statType: SubmissionStatType,
    ): DocSubmissionStats?

    @Query("{ 'stats.?0': { \$exists: true } }")
    fun findAllByStatType(
        statType: SubmissionStatType,
        pageable: Pageable,
    ): Flow<DocSubmissionStats>
}

interface SubmissionMongoRepository : CoroutineCrudRepository<DocSubmission, ObjectId> {
    @Query("{ 'accNo': '?0', 'version': { \$gte: 0 } }")
    suspend fun findByAccNo(accNo: String): DocSubmission?

    suspend fun getByAccNoAndVersion(
        accNo: String,
        version: Int,
    ): DocSubmission

    suspend fun existsByAccNo(accNo: String): Boolean

    suspend fun existsByAccNoAndVersion(
        accNo: String,
        version: Int,
    ): Boolean

    fun getByAccNoInAndVersionGreaterThan(
        accNo: List<String>,
        version: Int,
    ): Flow<DocSubmission>

    suspend fun findFirstByAccNoAndVersionLessThanOrderByVersion(
        accNo: String,
        version: Int = 0,
    ): DocSubmission?

    @Query(value = "{ 'accNo' : ?0, 'version' : { \$gt: 0} }", fields = "{ 'collections.accNo':1 }")
    suspend fun findSubmissionCollections(accNo: String): SubmissionCollections?
}

suspend fun SubmissionMongoRepository.getByAccNo(accNo: String): DocSubmission {
    return findByAccNo(accNo) ?: throw SubmissionNotFoundException(accNo)
}

interface SubmissionRequestRepository : CoroutineCrudRepository<DocSubmissionRequest, String> {
    suspend fun existsByAccNoAndStatusIn(
        accNo: String,
        status: Set<RequestStatus>,
    ): Boolean

    suspend fun existsByAccNoAndVersionAndStatus(
        accNo: String,
        version: Int,
        status: RequestStatus,
    ): Boolean

    suspend fun getByAccNoAndStatusIn(
        accNo: String,
        status: Set<RequestStatus>,
    ): DocSubmissionRequest

    suspend fun getByAccNoAndVersion(
        accNo: String,
        version: Int,
    ): DocSubmissionRequest

    fun findByStatusIn(status: Set<RequestStatus>): Flow<DocSubmissionRequest>

    fun findByStatusInAndModificationTimeLessThan(
        status: Set<RequestStatus>,
        since: Instant,
    ): Flow<DocSubmissionRequest>

    suspend fun getById(id: ObjectId): DocSubmissionRequest

    suspend fun findByAccNo(accNo: String): Flow<DocSubmissionRequest>
}

interface SubmissionRequestFilesRepository : CoroutineCrudRepository<DocSubmissionRequestFile, ObjectId> {
    /**
     * Get the submission request files. Note that as some operation may take signifcant amoount of time
     * (like calculating md5 of a large file) no timeout cursor is used.
     */
    @Query("{ 'accNo': ?0, 'version': ?1, 'index': { \$gt: ?2 } }", sort = "{ index: 1 }")
    @Meta(flags = [CursorOption.NO_TIMEOUT])
    fun findRequestFiles(
        accNo: String,
        version: Int,
        index: Int,
    ): Flow<DocSubmissionRequestFile>

    @Query("{ 'accNo': ?0, 'version': ?1, 'status': ?2 }")
    @Meta(flags = [CursorOption.NO_TIMEOUT])
    fun findRequestFiles(
        accNo: String,
        version: Int,
        status: RequestFileStatus,
    ): Flow<DocSubmissionRequestFile>

    @Query("{ 'path': ?0, 'accNo': ?1, 'version': ?2, 'previousSubFile': false }")
    suspend fun getByPathAndAccNoAndVersion(
        path: String,
        accNo: String,
        version: Int,
    ): DocSubmissionRequestFile
}

interface FileListDocFileRepository : CoroutineCrudRepository<FileListDocFile, ObjectId> {
    fun findAllBySubmissionAccNoAndSubmissionVersionGreaterThanAndFileListNameOrderByIndexAsc(
        accNo: String,
        version: Int,
        fileListName: String,
    ): Flow<FileListDocFile>

    fun findAllBySubmissionAccNoAndSubmissionVersionAndFileListNameOrderByIndexAsc(
        accNo: String,
        version: Int,
        fileListName: String,
    ): Flow<FileListDocFile>

    @Query("{ 'submissionAccNo': ?0, 'submissionVersion': ?1, 'file.filePath': ?2}")
    suspend fun findBySubmissionAccNoAndSubmissionVersionAndFilePath(
        accNo: String,
        version: Int,
        filePath: String,
    ): Flow<FileListDocFile>
}
