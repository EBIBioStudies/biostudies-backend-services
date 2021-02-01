package ac.uk.ebi.biostd.persistence.doc.db.data

import ac.uk.ebi.biostd.persistence.doc.commons.ExtendedUpdate
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_STATS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_STATUS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_VERSION
import ac.uk.ebi.biostd.persistence.doc.db.repositories.SubmissionMongoRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocProcessingStatus
import ac.uk.ebi.biostd.persistence.doc.model.DocProject
import ac.uk.ebi.biostd.persistence.doc.model.DocStat
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
        updateSubmissionField(accNo, version, SUB_STATUS, status)
    }

    fun updateStats(accNo: String, version: Int, stats: List<DocStat>) {
        updateSubmissionField(accNo, version, SUB_STATS, stats)
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

    fun deleteSubmissionDrafts(submitter: String, accNo: String) {
        TODO()
    }

    fun getProjects(accNo: String): List<DocProject> = submissionRepository.getSubmissionProjects(accNo).projects

    private fun updateSubmissionField(accNo: String, version: Int, field: String, value: Any) {
        val query = Query(where(SUB_ACC_NO).`is`(accNo).andOperator(where(SUB_VERSION).`is`(version)))
        val update = update(field, value)
        mongoTemplate.updateFirst(query, update, DocSubmission::class.java)
    }
}

data class Result(
    val id: String,
    val maxVersion: Int
)
