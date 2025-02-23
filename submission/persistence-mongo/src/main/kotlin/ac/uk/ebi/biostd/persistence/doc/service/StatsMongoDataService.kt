package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.exception.StatNotFoundException
import ac.uk.ebi.biostd.persistence.common.exception.StatsNotFoundException
import ac.uk.ebi.biostd.persistence.common.exception.SubmissionNotFoundException
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStat
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStats
import ac.uk.ebi.biostd.persistence.common.request.PageRequest
import ac.uk.ebi.biostd.persistence.common.service.StatsDataService
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocStatsFields.STATS_STATS_MAP
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionStatsDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.reactive.repositories.SubmissionMongoRepository
import com.mongodb.bulk.BulkWriteResult
import com.mongodb.client.model.Filters
import com.mongodb.client.model.UpdateOneModel
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.Updates
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.bson.Document
import java.time.Instant
import org.springframework.data.domain.PageRequest as DataPageRequest

@Suppress("TooManyFunctions")
class StatsMongoDataService(
    private val submissionsRepository: SubmissionMongoRepository,
    private val statsDataRepository: SubmissionStatsDataRepository,
) : StatsDataService {
    override suspend fun findByAccNo(accNo: String): SubmissionStats? =
        statsDataRepository
            .findByAccNo(accNo)
            ?.let {
                SubmissionStats(
                    it.accNo,
                    it.stats.map { SubmissionStat(accNo, it.key, it.value) },
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

    override suspend fun saveStat(stat: SubmissionStat): SubmissionStat {
        require(submissionsRepository.existsByAccNo(stat.accNo)) { throw SubmissionNotFoundException(stat.accNo) }
        statsDataRepository.updateOrRegisterStat(stat)
        val updated = statsDataRepository.getByAccNo(stat.accNo)
        return SubmissionStat(updated.accNo, updated.stats[stat.type.name]!!, stat.type)
    }

    override suspend fun saveSubmissionStats(
        accNo: String,
        stats: List<SubmissionStat>,
    ): List<SubmissionStat> {
        val submissionStats = stats.associateBy({ it.type.name }, { it.value })
        return statsDataRepository
            .upsertStats(accNo, submissionStats)
            .stats
            .map { SubmissionStat(accNo = accNo, type = SubmissionStatType.fromString(it.key), value = it.value) }
    }

    override suspend fun saveAll(stats: List<SubmissionStat>): BulkWriteResult {
        val upserts =
            stats
                .map {
                    UpdateOneModel<Document>(
                        Filters.eq("accNo", it.accNo),
                        Updates.set("$STATS_STATS_MAP.${it.type}", it.value),
                        UpdateOptions().upsert(true),
                    )
                }
        return statsDataRepository.bulkWrite(upserts)
    }

    override suspend fun lastUpdated(accNo: String): Instant? = statsDataRepository.findByAccNo(accNo)?.lastUpdated

    override suspend fun deleteByAccNo(accNo: String) {
        statsDataRepository.deleteByAccNo(accNo)
    }

    override suspend fun incrementAll(stats: List<SubmissionStat>): BulkWriteResult {
        val upserts =
            stats.map {
                UpdateOneModel<Document>(
                    Filters.eq("accNo", it.accNo),
                    Updates.inc("$STATS_STATS_MAP.${it.type}", it.value),
                    UpdateOptions().upsert(true),
                )
            }
        return statsDataRepository.bulkWrite(upserts)
    }
}
