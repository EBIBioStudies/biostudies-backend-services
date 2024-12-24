package ac.uk.ebi.biostd.persistence.doc.db.data

import ac.uk.ebi.biostd.persistence.common.model.SubmissionStat
import ac.uk.ebi.biostd.persistence.doc.commons.collection
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_STATS
import ac.uk.ebi.biostd.persistence.doc.db.reactive.repositories.SubmissionStatsRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionStats
import com.mongodb.client.model.UpdateOneModel
import com.mongodb.client.model.UpdateOptions
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.bson.Document
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update.update
import reactor.core.publisher.Mono

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
                update("$SUB_STATS", stats),
                FindAndModifyOptions().returnNew(true).upsert(true),
                DocSubmissionStats::class.java,
            ).awaitSingle()
    }

    suspend fun updateOrRegisterStat(stat: SubmissionStat) {
        val query = Query(where(DocSubmissionFields.SUB_ACC_NO).`is`(stat.accNo))
        mongoTemplate
            .upsert(query, update("$SUB_STATS.${stat.type}", stat.value), DocSubmissionStats::class.java)
            .awaitSingleOrNull()
    }

    suspend fun incrementStat(
        accNo: String,
        stats: List<SubmissionStat>,
    ) {
        val operations =
            stats
                .stream()
                .map { stat ->
                    val update = Document("$SUB_STATS.${stat.type}", stat.value)
                    val filter = Document(DocSubmissionFields.SUB_ACC_NO, accNo)
                    UpdateOneModel<Document>(filter, Document("\$inc", update), UpdateOptions().upsert(true))
                }.toList()

        val result = Mono.from(mongoTemplate.collection<DocSubmissionStats>().bulkWrite(operations))
        result.awaitSingleOrNull()
    }
}
