package ac.uk.ebi.biostd.persistence.doc.db.data

import ac.uk.ebi.biostd.persistence.common.model.SubmissionStat
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.STAT_DOC_NAME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.STAT_DOC_VALUE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_STATS
import ac.uk.ebi.biostd.persistence.doc.db.repositories.SubmissionMongoRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import org.springframework.data.mongodb.core.BulkOperations
import org.springframework.data.mongodb.core.BulkOperations.BulkMode.UNORDERED
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.query.Update.update

class SubmissionStatsDataRepository(
    private val mongoTemplate: MongoTemplate,
    private val submissionRepository: SubmissionMongoRepository
) : SubmissionMongoRepository by submissionRepository {
    fun updateStat(accNo: String, version: Int, stat: SubmissionStat) {
        val query = findStatsQuery(accNo, version, stat.type.name)
        mongoTemplate.updateFirst(query, update("$SUB_STATS.$.$STAT_DOC_VALUE", stat.value), DocSubmission::class.java)
    }

    fun incrementStat(accNo: String, version: Int, stats: List<SubmissionStat>): Long {
        var increment = 0L
        val bulk = mongoTemplate.bulkOps(UNORDERED, DocSubmission::class.java)

        stats.forEach {
            bulk.incrementStat(accNo, version, it)
            increment += it.value
        }

        bulk.execute()
        return increment
    }

    private fun findStatsQuery(accNo: String, version: Int, name: String) =
        Query(
            where(DocSubmissionFields.SUB_ACC_NO).`is`(accNo).andOperator(
                where(DocSubmissionFields.SUB_VERSION).`is`(version),
                where("$SUB_STATS.$STAT_DOC_NAME").`is`(name)
            )
        )

    private fun BulkOperations.incrementStat(accNo: String, version: Int, stat: SubmissionStat) =
        updateOne(
            findStatsQuery(accNo, version, stat.type.name),
            Update().inc("$SUB_STATS.$.$STAT_DOC_VALUE", stat.value)
        )
}
