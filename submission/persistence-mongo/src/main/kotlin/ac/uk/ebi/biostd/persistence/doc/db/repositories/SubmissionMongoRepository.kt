package ac.uk.ebi.biostd.persistence.doc.db.repositories

import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.SubmissionRequest
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query

interface SubmissionMongoRepository : MongoRepository<DocSubmission, String> {
    @Query("{ 'accNo': '?0', 'version': { \$gte: 0 } }")
    fun findByAccNo(accNo: String): DocSubmission?

    fun existsByAccNo(accNo: String): Boolean

    @Query("{ 'accNo': '?0', 'version': { \$gte: 0 } }")
    fun getByAccNo(accNo: String): DocSubmission

    fun getByAccNoAndVersion(accNo: String, version: Int): DocSubmission

    fun getByAccNoAndVersionGreaterThan(accNo: String, version: Int): List<DocSubmission>

    fun findFirstByAccNoOrderByVersionDesc(accNo: String): DocSubmission?

    @Query(value = "{ 'accNo' : ?0 }", fields = "{ 'projects.accNo':1 }")
    fun getSubmissionProjects(accNo: String): SubmissionProjects

    @Query("{ 'accNo': '?0', 'stats.name': { \$eq: '?1' } }")
    fun findByAccNoAndStatType(accNo: String, statType: SubmissionStatType): DocSubmission?

    @Query("{ 'stats.name': { \$eq: '?0' } }")
    fun findAllByStatType(statType: SubmissionStatType, pageable: Pageable): Page<DocSubmission>
}

interface SubmissionRequestRepository : MongoRepository<SubmissionRequest, String>
