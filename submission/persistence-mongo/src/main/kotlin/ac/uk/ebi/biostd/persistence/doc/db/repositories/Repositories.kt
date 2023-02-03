package ac.uk.ebi.biostd.persistence.doc.db.repositories

import ac.uk.ebi.biostd.persistence.common.exception.SubmissionNotFoundException
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft.DraftStatus
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequest
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequestFile
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionStats
import ac.uk.ebi.biostd.persistence.doc.model.FileListDocFile
import org.bson.types.ObjectId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.query.Meta.CursorOption
import org.springframework.data.mongodb.repository.Meta
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import java.time.Instant
import java.util.stream.Stream

interface SubmissionMongoRepository : MongoRepository<DocSubmission, ObjectId> {
    @Query(value = "{ 'accNo': '?0', 'version': { \$gte: 0 } }", fields = "{ relPath : 1, released: 2 }")
    fun getSubData(accNo: String): SubmissionProjection

    @Query("{ 'accNo': '?0', 'version': { \$gte: 0 } }")
    fun findByAccNo(accNo: String): DocSubmission?

    fun existsByAccNo(accNo: String): Boolean

    fun existsByAccNoAndVersion(accNo: String, version: Int): Boolean

    fun getByAccNoAndVersion(accNo: String, version: Int): DocSubmission

    fun getByAccNoInAndVersionGreaterThan(accNo: List<String>, version: Int): List<DocSubmission>

    fun findFirstByAccNoAndVersionLessThanOrderByVersion(accNo: String, version: Int = 0): DocSubmission?

    @Query(value = "{ 'accNo' : ?0, 'version' : { \$gt: 0} }", fields = "{ 'collections.accNo':1 }")
    fun findSubmissionCollections(accNo: String): SubmissionCollections?
}

fun SubmissionMongoRepository.getByAccNo(accNo: String) = findByAccNo(accNo) ?: throw SubmissionNotFoundException(accNo)

interface SubmissionRequestRepository : MongoRepository<DocSubmissionRequest, String> {
    fun getByAccNoAndVersionAndStatus(
        accNo: String,
        version: Int,
        status: RequestStatus,
    ): DocSubmissionRequest

    fun existsByAccNoAndStatusIn(accNo: String, status: Set<RequestStatus>): Boolean
    fun getByAccNoAndStatusIn(accNo: String, status: Set<RequestStatus>): DocSubmissionRequest

    fun getByAccNoAndVersion(accNo: String, version: Int): DocSubmissionRequest

    fun findByStatusIn(status: Set<RequestStatus>): List<DocSubmissionRequest>

    fun findByStatusInAndModificationTimeLessThan(
        status: Set<RequestStatus>,
        since: Instant,
    ): List<DocSubmissionRequest>

    fun getById(id: ObjectId): DocSubmissionRequest
    fun findByAccNo(accNo: String): List<DocSubmissionRequest>
}

interface SubmissionRequestFilesRepository : MongoRepository<DocSubmissionRequestFile, ObjectId> {
    @Meta(cursorBatchSize = 100, flags = [CursorOption.NO_TIMEOUT])
    fun findAllByAccNoAndVersionAndIndexGreaterThan(
        accNo: String,
        version: Int,
        index: Int,
    ): Stream<DocSubmissionRequestFile>

    fun getByPathAndAccNoAndVersion(path: String, accNo: String, version: Int): DocSubmissionRequestFile
}

interface SubmissionDraftRepository : MongoRepository<DocSubmissionDraft, String> {
    fun findByUserIdAndKeyAndStatusIsNot(userId: String, key: String, deleted: DraftStatus): DocSubmissionDraft?

    fun findAllByUserIdAndStatus(
        userId: String,
        status: DraftStatus,
        pageRequest: Pageable,
    ): List<DocSubmissionDraft>

    fun getById(id: String): DocSubmissionDraft

    fun deleteByUserIdAndKey(userId: String, draftKey: String)
}

interface FileListDocFileRepository : MongoRepository<FileListDocFile, ObjectId> {
    fun findAllBySubmissionAccNoAndSubmissionVersionGreaterThanAndFileListName(
        accNo: String,
        version: Int,
        fileListName: String,
    ): List<FileListDocFile>

    fun findAllBySubmissionAccNoAndSubmissionVersionAndFileListName(
        accNo: String,
        version: Int,
        fileListName: String,
    ): List<FileListDocFile>

    @Query("{ 'submissionAccNo': ?0, 'submissionVersion': ?1, 'file.filePath': ?2}")
    fun findBySubmissionAccNoAndSubmissionVersionAndFilePath(
        accNo: String,
        version: Int,
        filePath: String,
    ): List<FileListDocFile>
}

interface SubmissionStatsRepository : MongoRepository<DocSubmissionStats, ObjectId> {
    fun getByAccNo(accNo: String): DocSubmissionStats

    fun findByAccNo(accNo: String): DocSubmissionStats?

    @Query("{ 'accNo': '?0', 'stats.?1': { \$exists: true } }")
    fun findByAccNoAndStatType(accNo: String, statType: SubmissionStatType): DocSubmissionStats?

    @Query("{ 'stats.?0': { \$exists: true } }")
    fun findAllByStatType(statType: SubmissionStatType, pageable: Pageable): Page<DocSubmissionStats>
}
