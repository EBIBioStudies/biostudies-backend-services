package ac.uk.ebi.biostd.persistence.doc.db.reactive.repositories

import ac.uk.ebi.biostd.persistence.common.exception.SubmissionNotFoundException
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType
import ac.uk.ebi.biostd.persistence.doc.db.repositories.SubmissionCollections
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequest
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequestFile
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionStats
import ac.uk.ebi.biostd.persistence.doc.model.FileListDocFile
import org.bson.types.ObjectId
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.Meta
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import java.time.Instant

interface SubmissionDraftRepository : ReactiveCrudRepository<DocSubmissionDraft, String> {
    suspend fun findByUserIdAndKeyAndStatusIsNot(
        userId: String,
        key: String,
        deleted: DocSubmissionDraft.DraftStatus,
    ): DocSubmissionDraft?

    fun findAllByUserIdAndStatus(
        userId: String,
        status: DocSubmissionDraft.DraftStatus,
        pageRequest: Pageable,
    ): Flux<DocSubmissionDraft>

    suspend fun getById(id: String): DocSubmissionDraft

    suspend fun deleteByUserIdAndKey(userId: String, draftKey: String)
}

interface SubmissionStatsRepository : ReactiveCrudRepository<DocSubmissionStats, ObjectId> {
    suspend fun getByAccNo(accNo: String): DocSubmissionStats

    suspend fun findByAccNo(accNo: String): DocSubmissionStats?

    @Query("{ 'accNo': '?0', 'stats.?1': { \$exists: true } }")
    suspend fun findByAccNoAndStatType(accNo: String, statType: SubmissionStatType): DocSubmissionStats?

    @Query("{ 'stats.?0': { \$exists: true } }")
    fun findAllByStatType(statType: SubmissionStatType, pageable: Pageable): Flux<DocSubmissionStats>
}

interface SubmissionMongoRepository : ReactiveCrudRepository<DocSubmission, ObjectId> {
    @Query("{ 'accNo': '?0', 'version': { \$gte: 0 } }")
    suspend fun findByAccNo(accNo: String): DocSubmission?

    suspend fun existsByAccNo(accNo: String): Boolean

    suspend fun existsByAccNoAndVersion(accNo: String, version: Int): Boolean

    suspend fun getByAccNoAndVersion(accNo: String, version: Int): DocSubmission

    fun getByAccNoInAndVersionGreaterThan(accNo: List<String>, version: Int): Flux<DocSubmission>

    suspend fun findFirstByAccNoAndVersionLessThanOrderByVersion(accNo: String, version: Int = 0): DocSubmission?

    @Query(value = "{ 'accNo' : ?0, 'version' : { \$gt: 0} }", fields = "{ 'collections.accNo':1 }")
    suspend fun findSubmissionCollections(accNo: String): SubmissionCollections?
}

suspend fun SubmissionMongoRepository.getByAccNo(accNo: String): DocSubmission =
    findByAccNo(accNo) ?: throw SubmissionNotFoundException(accNo)

interface SubmissionRequestRepository : ReactiveCrudRepository<DocSubmissionRequest, String> {
    suspend fun getByAccNoAndVersionAndStatus(
        accNo: String,
        version: Int,
        status: RequestStatus,
    ): DocSubmissionRequest

    suspend fun existsByAccNoAndStatusIn(accNo: String, status: Set<RequestStatus>): Boolean
    suspend fun getByAccNoAndStatusIn(accNo: String, status: Set<RequestStatus>): DocSubmissionRequest

    suspend fun getByAccNoAndVersion(accNo: String, version: Int): DocSubmissionRequest

    fun findByStatusIn(status: Set<RequestStatus>): Flux<DocSubmissionRequest>

    fun findByStatusInAndModificationTimeLessThan(
        status: Set<RequestStatus>,
        since: Instant,
    ): Flux<DocSubmissionRequest>

    suspend fun getById(id: ObjectId): DocSubmissionRequest
    suspend fun findByAccNo(accNo: String): Flux<DocSubmissionRequest>
}

interface SubmissionRequestFilesRepository : ReactiveCrudRepository<DocSubmissionRequestFile, ObjectId> {
    @Query("{ 'accNo': ?0, 'version': ?1, 'index': { \$gt: ?2 } }", sort = "{ index: 1 }")
    fun findRequestFiles(
        accNo: String,
        version: Int,
        index: Int,
    ): Flux<DocSubmissionRequestFile>

    suspend fun getByPathAndAccNoAndVersion(path: String, accNo: String, version: Int): DocSubmissionRequestFile
}

interface FileListDocFileRepository : ReactiveCrudRepository<FileListDocFile, ObjectId> {
    @Meta(cursorBatchSize = 100, flags = [org.springframework.data.mongodb.core.query.Meta.CursorOption.NO_TIMEOUT])
    fun findAllBySubmissionAccNoAndSubmissionVersionGreaterThanAndFileListName(
        accNo: String,
        version: Int,
        fileListName: String,
    ): Flux<FileListDocFile>

    fun findAllBySubmissionAccNoAndSubmissionVersionAndFileListName(
        accNo: String,
        version: Int,
        fileListName: String,
    ): Flux<FileListDocFile>

    @Query("{ 'submissionAccNo': ?0, 'submissionVersion': ?1, 'file.filePath': ?2}")
    fun findBySubmissionAccNoAndSubmissionVersionAndFilePath(
        accNo: String,
        version: Int,
        filePath: String,
    ): Flux<FileListDocFile>
}
