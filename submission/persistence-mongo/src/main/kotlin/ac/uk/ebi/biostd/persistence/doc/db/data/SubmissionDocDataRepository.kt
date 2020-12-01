package ac.uk.ebi.biostd.persistence.doc.db.data

import ac.uk.ebi.biostd.persistence.doc.db.repositories.SubmissionMongoRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocProcessingStatus
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation.group
import org.springframework.data.mongodb.core.aggregation.Aggregation.match
import org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation
import org.springframework.data.mongodb.core.aggregation.Aggregation.sort
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update

class SubmissionDocDataRepository(
    private val submissionRepository: SubmissionMongoRepository,
    private val mongoTemplate: MongoTemplate
) {
    fun updateStatus(status: DocProcessingStatus, accNo: String, version: Int) {
        val query = Query(
            where("accNo").`is`(accNo).andOperator(
                where("version").`is`(version)))
        val update = Update.update("status", status)
        mongoTemplate.updateFirst(query, update, DocSubmission::class.java)
    }

    fun save(docSubmission: DocSubmission) {
        submissionRepository.save(docSubmission)
    }

    fun getCurrentVersion(accNo: String): Int? {
        val aggregation = newAggregation(
            DocSubmission::class.java,
            match(where("accNo").`is`(accNo)),
            group("accNo").max("version").`as`("maxVersion"),
            sort(Sort.Direction.DESC, "maxVersion")
        )
        return mongoTemplate.aggregate(aggregation, Result::class.java).uniqueMappedResult?.maxVersion
    }
}

data class Result(
    val id: String,
    val maxVersion: Int
)
