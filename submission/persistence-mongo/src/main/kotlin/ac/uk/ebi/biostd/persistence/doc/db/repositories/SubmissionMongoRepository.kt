package ac.uk.ebi.biostd.persistence.doc.db.repositories

import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query

interface SubmissionMongoRepository : MongoRepository<DocSubmission, String> {

    fun existsByAccNo(accNo: String): Boolean

    @Query(value = "{ 'groupId' :  ?0 }", fields = "{ '_id': 0, 'user.\$id':1 }")
    fun findAllUserIdByGroupId(accNo: String): List<String?>?
}
