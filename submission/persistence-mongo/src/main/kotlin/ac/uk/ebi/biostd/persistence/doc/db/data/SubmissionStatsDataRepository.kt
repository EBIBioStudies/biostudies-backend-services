package ac.uk.ebi.biostd.persistence.doc.db.data

import ac.uk.ebi.biostd.persistence.common.model.SubmissionStat
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_STATS
import ac.uk.ebi.biostd.persistence.doc.db.repositories.SubmissionStatsRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionStats
import org.springframework.data.mongodb.core.BulkOperations
import org.springframework.data.mongodb.core.BulkOperations.BulkMode.UNORDERED
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.query.Update.update

class SubmissionStatsDataRepository(
    private val mongoTemplate: MongoTemplate,
    private val statsRepository: SubmissionStatsRepository,
) : SubmissionStatsRepository by statsRepository {
    fun updateOrRegisterStat(stat: SubmissionStat) {
        val query = findStatsQuery(stat.accNo)
        mongoTemplate.upsert(query, update("$SUB_STATS.${stat.type}", stat.value), DocSubmissionStats::class.java)
    }

    fun incrementStat(accNo: String, stats: List<SubmissionStat>): Long {
        var increment = 0L
        val bulk = mongoTemplate.bulkOps(UNORDERED, DocSubmissionStats::class.java)

        stats.forEach {
            bulk.incrementStat(accNo, it)
            increment += it.value
        }

        bulk.execute()
        return increment
    }

    private fun findStatsQuery(accNo: String) = Query(where(DocSubmissionFields.SUB_ACC_NO).`is`(accNo))

    private fun BulkOperations.incrementStat(accNo: String, stat: SubmissionStat) =
        upsert(
            findStatsQuery(accNo),
            Update().inc("$SUB_STATS.${stat.type}", stat.value)
        )
}
