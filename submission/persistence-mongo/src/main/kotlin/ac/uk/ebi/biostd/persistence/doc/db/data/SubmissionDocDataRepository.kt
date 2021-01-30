package ac.uk.ebi.biostd.persistence.doc.db.data

import ac.uk.ebi.biostd.persistence.doc.commons.ExtendedUpdate
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_STATUS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_VERSION
import ac.uk.ebi.biostd.persistence.doc.db.repositories.SubmissionMongoRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocProcessingStatus
import ac.uk.ebi.biostd.persistence.doc.model.DocProject
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation.group
import org.springframework.data.mongodb.core.aggregation.Aggregation.match
import org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation
import org.springframework.data.mongodb.core.aggregation.Aggregation.sort
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update.update

class SubmissionDocDataRepository(
    private val submissionRepository: SubmissionMongoRepository,
    private val mongoTemplate: MongoTemplate
) : SubmissionMongoRepository by submissionRepository {
    fun updateStatus(status: DocProcessingStatus, accNo: String, version: Int) {
        val query = Query(where(SUB_ACC_NO).`is`(accNo).andOperator(where(SUB_VERSION).`is`(version)))
        val update = update(SUB_STATUS, status)
        mongoTemplate.updateFirst(query, update, DocSubmission::class.java)
    }

    fun getCurrentVersion(accNo: String): Int? {
        val aggregation = newAggregation(
            DocSubmission::class.java,
            match(where(SUB_ACC_NO).`is`(accNo)),
            group(SUB_ACC_NO).max(SUB_VERSION).`as`("maxVersion"),
            sort(Sort.Direction.DESC, "maxVersion")
        )
        return mongoTemplate.aggregate(aggregation, Result::class.java).uniqueMappedResult?.maxVersion
    }

    fun expireActiveProcessedVersions(accNo: String) {
        val criteria = where(SUB_ACC_NO).`is`(accNo).andOperator(
            where(SUB_VERSION).gt(0),
            where(SUB_STATUS).`is`(DocProcessingStatus.PROCESSED)
        )
        mongoTemplate.updateMulti(
            Query(criteria),
            ExtendedUpdate().multiply(SUB_VERSION, -1),
            DocSubmission::class.java)
    }

    fun getProjects(accNo: String): List<DocProject> = submissionRepository.getSubmissionProjects(accNo).projects
}

data class Result(
    val id: String,
    val maxVersion: Int
)
