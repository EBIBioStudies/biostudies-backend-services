package ac.uk.ebi.biostd.persistence.doc.db.data

import ac.uk.ebi.biostd.persistence.doc.commons.collection
import ac.uk.ebi.biostd.persistence.doc.db.reactive.repositories.SubmissionStatsRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionStats
import com.mongodb.bulk.BulkWriteResult
import com.mongodb.client.model.UpdateOneModel
import kotlinx.coroutines.reactive.awaitSingle
import org.bson.Document
import org.springframework.data.mongodb.core.ReactiveMongoTemplate

class SubmissionStatsDataRepository(
    private val mongoTemplate: ReactiveMongoTemplate,
    private val statsRepository: SubmissionStatsRepository,
) : SubmissionStatsRepository by statsRepository {
    suspend fun bulkWrite(operations: List<UpdateOneModel<Document>>): BulkWriteResult =
        mongoTemplate.collection<DocSubmissionStats>().bulkWrite(operations).awaitSingle()
}
