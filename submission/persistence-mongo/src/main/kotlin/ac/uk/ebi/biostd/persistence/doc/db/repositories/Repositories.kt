package ac.uk.ebi.biostd.persistence.doc.db.repositories

import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft
import ac.uk.ebi.biostd.persistence.doc.model.FileListDocFile
import ac.uk.ebi.biostd.persistence.doc.model.SubmissionRequest
import org.bson.types.ObjectId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query

interface SubmissionMongoRepository : MongoRepository<DocSubmission, ObjectId> {
    @Query("{ 'accNo': '?0', 'version': { \$gte: 0 } }")
    fun findByAccNo(accNo: String): DocSubmission?

    @Query("{ 'accNo': '?0', 'version': { \$gte: 0 } }")
    fun getByAccNo(accNo: String): DocSubmission

    fun existsByAccNo(accNo: String): Boolean

    fun getByAccNoAndVersion(accNo: String, version: Int): DocSubmission

    fun getByAccNoAndVersionGreaterThan(accNo: String, version: Int): DocSubmission

    fun getByAccNoInAndVersionGreaterThan(accNo: List<String>, version: Int): List<DocSubmission>

    fun findFirstByAccNoOrderByVersionDesc(accNo: String): DocSubmission?

    @Query(value = "{ 'accNo' : ?0, 'version' : { \$gt: 0} }", fields = "{ 'collections.accNo':1 }")
    fun findSubmissionCollections(accNo: String): SubmissionProjects?

    @Query("{ 'accNo': '?0', 'stats.name': { \$eq: '?1' } }")
    fun findByAccNoAndStatType(accNo: String, statType: SubmissionStatType): DocSubmission?

    @Query("{ 'stats.name': { \$eq: '?0' } }")
    fun findAllByStatType(statType: SubmissionStatType, pageable: Pageable): Page<DocSubmission>
}

interface SubmissionRequestRepository : MongoRepository<SubmissionRequest, String> {
    fun getByAccNoAndVersion(accNo: String, version: Int): SubmissionRequest
}

interface SubmissionDraftRepository : MongoRepository<DocSubmissionDraft, String> {
    fun findByUserIdAndKey(userId: String, key: String): DocSubmissionDraft?

    fun findAllByUserId(userId: String, pageRequest: Pageable): List<DocSubmissionDraft>

    fun getById(id: String): DocSubmissionDraft

    fun deleteByKey(key: String)

    fun deleteByUserIdAndKey(userId: String, key: String)
}

interface FileListDocFileRepository : MongoRepository<FileListDocFile, ObjectId>
