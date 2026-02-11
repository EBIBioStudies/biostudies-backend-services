package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.exception.StatNotFoundException
import ac.uk.ebi.biostd.persistence.common.exception.StatsNotFoundException
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStat
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStats
import ac.uk.ebi.biostd.persistence.common.request.PageRequest
import ac.uk.ebi.biostd.persistence.common.service.StatsDataService
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocStatsFields.STATS_COLLECTIONS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocStatsFields.STATS_LAST_UPDATED
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocStatsFields.STATS_RELEASED
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocStatsFields.STATS_STATS_MAP
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocStatsFields.STATS_STORAGE_MODE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocStatsFields.STATS_SUB_CREATION_TIME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocStatsFields.STATS_SUB_MODIFICATION_TIME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocStatsFields.STATS_VERSION
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionStatsDataRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionStats
import com.mongodb.bulk.BulkWriteResult
import com.mongodb.client.model.Filters
import com.mongodb.client.model.UpdateOneModel
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.Updates
import ebi.ac.uk.extended.model.ExtSubmission
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.bson.Document
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import java.time.Instant
import org.springframework.data.domain.PageRequest as DataPageRequest

@Suppress("TooManyFunctions")
class StatsMongoDataService(
    private val statsDataRepository: SubmissionStatsDataRepository,
    private val mongoTemplate: ReactiveMongoTemplate,
) : StatsDataService {
    override fun findAll(lastUpdated: Instant): Flow<String> {
        suspend fun findNext(): DocSubmissionStats? {
            val query =
                Query(
                    Criteria().orOperator(
                        where(STATS_LAST_UPDATED).lt(lastUpdated),
                        where(STATS_LAST_UPDATED).isNull,
                    ),
                ).limit(1)
            val update = Update().set(STATS_LAST_UPDATED, Instant.now())
            return mongoTemplate
                .findAndModify(
                    query,
                    update,
                    FindAndModifyOptions.options().returnNew(true),
                    DocSubmissionStats::class.java,
                ).awaitSingleOrNull()
        }

        return flow {
            while (true) {
                val doc = findNext() ?: break
                emit(doc.accNo)
            }
        }
    }

    override suspend fun findByAccNo(accNo: String): SubmissionStats? =
        statsDataRepository
            .findByAccNo(accNo)
            ?.let {
                SubmissionStats(
                    it.accNo,
                    it.stats.map { stat -> SubmissionStat(accNo, stat.key, stat.value) },
                )
            }

    override suspend fun findStatsByAccNo(accNo: String): List<SubmissionStat> =
        statsDataRepository
            .findByAccNo(accNo)
            ?.stats
            ?.map { SubmissionStat(accNo, it.key, it.value) }
            ?: throw StatsNotFoundException(accNo)

    override fun findStatsByType(
        submissionStatType: SubmissionStatType,
        filter: PageRequest,
    ): Flow<SubmissionStat> =
        statsDataRepository
            .findAllByStatType(submissionStatType, DataPageRequest.of(filter.pageNumber, filter.limit))
            .map { SubmissionStat(it.accNo, it.stats[submissionStatType.name]!!, submissionStatType) }

    override suspend fun findStatByAccNoAndType(
        accNo: String,
        submissionStatType: SubmissionStatType,
    ): SubmissionStat =
        statsDataRepository
            .findByAccNoAndStatType(accNo, submissionStatType)
            ?.let { SubmissionStat(it.accNo, it.stats[submissionStatType.name]!!, submissionStatType) }
            ?: throw StatNotFoundException(accNo, submissionStatType)

    override suspend fun saveAll(
        sub: ExtSubmission,
        stats: List<SubmissionStat>,
    ): BulkWriteResult {
        val collections = sub.collections.map { it.accNo }
        val upserts =
            stats
                .map {
                    UpdateOneModel<Document>(
                        Filters.eq("accNo", it.accNo),
                        listOf(
                            Updates.set(STATS_VERSION, sub.version),
                            Updates.set(STATS_RELEASED, sub.released),
                            Updates.set(STATS_COLLECTIONS, collections),
                            Updates.set(STATS_STORAGE_MODE, sub.storageMode),
                            Updates.set(STATS_SUB_CREATION_TIME, sub.creationTime.toInstant()),
                            Updates.set(STATS_SUB_MODIFICATION_TIME, sub.modificationTime.toInstant()),
                            Updates.set("$STATS_STATS_MAP.${it.type}", it.value),
                            Updates.set(STATS_LAST_UPDATED, Instant.now()),
                        ),
                        UpdateOptions().upsert(true),
                    )
                }

        return statsDataRepository.bulkWrite(upserts)
    }

    override suspend fun lastUpdated(accNo: String): Instant? = statsDataRepository.findByAccNo(accNo)?.lastUpdated

    override suspend fun deleteStatsByAccNo(accNo: String) {
        statsDataRepository.deleteAllByAccNo(accNo)
    }

    override suspend fun incrementAll(stats: List<SubmissionStat>): BulkWriteResult {
        val upserts =
            stats.map {
                UpdateOneModel<Document>(
                    Filters.eq("accNo", it.accNo),
                    Updates.inc("$STATS_STATS_MAP.${it.type}", it.value),
                    UpdateOptions().upsert(false),
                )
            }

        return statsDataRepository.bulkWrite(upserts)
    }
}
