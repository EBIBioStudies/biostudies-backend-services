package ac.uk.ebi.biostd.persistence.doc.db.repositories

import ac.uk.ebi.biostd.persistence.common.exception.SubmissionNotFoundException
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft.DraftStatus
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequest
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionStats
import ac.uk.ebi.biostd.persistence.doc.model.FileListDocFile
import ac.uk.ebi.biostd.persistence.doc.model.SubmissionRequestStatus
import org.bson.types.ObjectId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query

interface SubmissionMongoRepository : MongoRepository<DocSubmission, ObjectId> {
    @Query("{ 'accNo': '?0', 'version': { \$gte: 0 } }")
    fun findByAccNo(accNo: String): DocSubmission?

    fun existsByAccNo(accNo: String): Boolean

    fun getByAccNoAndVersion(accNo: String, version: Int): DocSubmission

    fun getByAccNoInAndVersionGreaterThan(accNo: List<String>, version: Int): List<DocSubmission>

    fun findFirstByAccNoOrderByVersionDesc(accNo: String): DocSubmission?

    @Query(value = "{ 'accNo' : ?0, 'version' : { \$gt: 0} }", fields = "{ 'collections.accNo':1 }")
    fun findSubmissionCollections(accNo: String): SubmissionProjects?
}

fun SubmissionMongoRepository.getByAccNo(accNo: String) = findByAccNo(accNo) ?: throw SubmissionNotFoundException(accNo)

interface SubmissionRequestRepository : MongoRepository<DocSubmissionRequest, String> {
    fun getByAccNoAndVersionAndStatus(
        accNo: String,
        version: Int,
        status: SubmissionRequestStatus
    ): DocSubmissionRequest

    fun existsByAccNoAndStatusIn(
        accNo: String,
        status: Set<SubmissionRequestStatus>
    ): Boolean
}

interface SubmissionDraftRepository : MongoRepository<DocSubmissionDraft, String> {
    fun findByUserIdAndKey(userId: String, key: String): DocSubmissionDraft?

    fun findAllByUserIdAndStatus(
        userId: String,
        status: DraftStatus,
        pageRequest: Pageable
    ): List<DocSubmissionDraft>

    fun getById(id: String): DocSubmissionDraft

    fun deleteByKey(key: String)

    fun deleteByUserIdAndKey(userId: String, key: String)
}

interface FileListDocFileRepository : MongoRepository<FileListDocFile, ObjectId> {
    fun findAllBySubmissionAccNoAndSubmissionVersionGreaterThanAndFileListName(
        accNo: String,
        version: Int,
        fileListName: String
    ): List<FileListDocFile>

    fun findAllBySubmissionAccNoAndSubmissionVersionAndFileListName(
        accNo: String,
        version: Int,
        fileListName: String
    ): List<FileListDocFile>
}

interface SubmissionStatsRepository : MongoRepository<DocSubmissionStats, ObjectId> {
    fun findByAccNo(accNo: String): DocSubmissionStats?

    @Query("{ 'accNo': '?0', 'stats.?1': { \$exists: true } }")
    fun findByAccNoAndStatType(accNo: String, statType: SubmissionStatType): DocSubmissionStats?

    @Query("{ 'stats.?0': { \$exists: true } }")
    fun findAllByStatType(statType: SubmissionStatType, pageable: Pageable): Page<DocSubmissionStats>
}
