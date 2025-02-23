package ac.uk.ebi.biostd.persistence.doc.db.data

import ac.uk.ebi.biostd.persistence.common.model.SubmissionStat
import ac.uk.ebi.biostd.persistence.doc.commons.collection
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocStatsFields.STATS_LAST_UPDATED
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocStatsFields.STATS_STATS_MAP
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields
import ac.uk.ebi.biostd.persistence.doc.db.reactive.repositories.SubmissionStatsRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionStats
import com.mongodb.bulk.BulkWriteResult
import com.mongodb.client.model.UpdateOneModel
import com.mongodb.client.model.UpdateOptions
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.bson.Document
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update.update
import reactor.core.publisher.Mono
import java.time.Instant

class SubmissionStatsDataRepository(
    private val mongoTemplate: ReactiveMongoTemplate,
    private val statsRepository: SubmissionStatsRepository,
) : SubmissionStatsRepository by statsRepository {
    suspend fun upsertStats(
        accNo: String,
        stats: Map<String, Long>,
    ): DocSubmissionStats {
        val query = Query(where(DocSubmissionFields.SUB_ACC_NO).`is`(accNo))
        return mongoTemplate
            .findAndModify(
                query,
                update(STATS_STATS_MAP, stats).set(STATS_LAST_UPDATED, Instant.now()),
                FindAndModifyOptions().returnNew(true).upsert(true),
                DocSubmissionStats::class.java,
            ).awaitSingle()
    }

    suspend fun updateOrRegisterStat(stat: SubmissionStat) {
        val query = Query(where(DocSubmissionFields.SUB_ACC_NO).`is`(stat.accNo))
        mongoTemplate
            .upsert(
                query,
                update("$STATS_STATS_MAP.${stat.type}", stat.value),
                DocSubmissionStats::class.java,
            ).awaitSingleOrNull()
    }

    suspend fun incrementStat(
        accNo: String,
        stats: List<SubmissionStat>,
    ) {
        val operations =
            stats
                .stream()
                .map { stat ->
                    val update = Document("$STATS_STATS_MAP.${stat.type}", stat.value)
                    val filter = Document(DocSubmissionFields.SUB_ACC_NO, accNo)
                    UpdateOneModel<Document>(filter, Document("\$inc", update), UpdateOptions().upsert(true))
                }.toList()

        val result = Mono.from(mongoTemplate.collection<DocSubmissionStats>().bulkWrite(operations))
        result.awaitSingleOrNull()
    }

    suspend fun bulkWrite(operations: List<UpdateOneModel<Document>>): BulkWriteResult {
        val result = mongoTemplate.collection<DocSubmissionStats>().bulkWrite(operations).awaitSingle()
        return result
    }
}
