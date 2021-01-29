package ac.uk.ebi.biostd.persistence.doc.db.repositories

import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft
import ac.uk.ebi.biostd.persistence.doc.model.SubmissionRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query

interface SubmissionMongoRepository : MongoRepository<DocSubmission, String> {

    fun findByAccNo(accNo: String): DocSubmission?

    fun existsByAccNo(accNo: String): Boolean

    fun getByAccNo(accNo: String): DocSubmission

    fun getByAccNoAndVersion(accNo: String, version: Int): DocSubmission

    fun getByAccNoAndVersionGreaterThan(accNo: String, version: Int): List<DocSubmission>

    fun findFirstByAccNoOrderByVersionDesc(accNo: String): DocSubmission?

    @Query(value = "{ 'accNo' : ?0 }", fields = "{ 'projects.accNo':1 }")
    fun getSubmissionProjects(accNo: String): SubmissionProjects
}

interface SubmissionRequestRepository : MongoRepository<SubmissionRequest, String>

interface SubmissionDraftRepository : MongoRepository<DocSubmissionDraft, String> {

    fun findByUserIdAndKey(userId: Long, key: String): DocSubmissionDraft?

    fun deleteByUserIdAndKey(userId: Long, key: String): Unit

    fun findAllByUserId(userId: Long, pageRequest: Pageable): List<DocSubmissionDraft>

    fun getById(id: String): DocSubmissionDraft
}
