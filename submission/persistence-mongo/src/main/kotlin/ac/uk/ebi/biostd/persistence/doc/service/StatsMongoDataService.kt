package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.exception.StatNotFoundException
import ac.uk.ebi.biostd.persistence.common.exception.StatsNotFoundException
import ac.uk.ebi.biostd.persistence.common.exception.SubmissionNotFoundException
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStat
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType
import ac.uk.ebi.biostd.persistence.common.request.PageRequest
import ac.uk.ebi.biostd.persistence.common.service.StatsDataService
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_STATS
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionStatsDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.reactive.repositories.SubmissionMongoRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionStats
import ac.uk.ebi.biostd.persistence.doc.model.SingleSubmissionStat
import com.mongodb.bulk.BulkWriteResult
import com.mongodb.client.model.Filters
import com.mongodb.client.model.UpdateOneModel
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.Updates
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.bson.Document
import org.springframework.data.domain.PageRequest as DataPageRequest

@Suppress("TooManyFunctions")
class StatsMongoDataService(
    private val submissionsRepository: SubmissionMongoRepository,
    private val statsDataRepository: SubmissionStatsDataRepository,
) : StatsDataService {
    override suspend fun findByAccNo(accNo: String): List<SubmissionStat> =
        statsDataRepository
            .findByAccNo(accNo)
            ?.stats
            ?.map { SingleSubmissionStat(accNo, it.value, SubmissionStatType.fromString(it.key)) }
            ?: throw StatsNotFoundException(accNo)

    override fun findByType(
        submissionStatType: SubmissionStatType,
        filter: PageRequest,
    ): Flow<SubmissionStat> =
        statsDataRepository
            .findAllByStatType(submissionStatType, DataPageRequest.of(filter.pageNumber, filter.limit))
            .map { toSubmissionStat(submissionStatType, it) }

    override suspend fun findByAccNoAndType(
        accNo: String,
        submissionStatType: SubmissionStatType,
    ): SubmissionStat =
        statsDataRepository
            .findByAccNoAndStatType(accNo, submissionStatType)
            ?.let { toSubmissionStat(submissionStatType, it) }
            ?: throw StatNotFoundException(accNo, submissionStatType)

    override suspend fun save(stat: SubmissionStat): SubmissionStat {
        require(submissionsRepository.existsByAccNo(stat.accNo)) { throw SubmissionNotFoundException(stat.accNo) }
        statsDataRepository.updateOrRegisterStat(stat)
        val updated = statsDataRepository.getByAccNo(stat.accNo)
        return toSubmissionStat(stat.type, updated)
    }

    override suspend fun saveSubmissionStats(
        accNo: String,
        stats: List<SubmissionStat>,
    ): List<SubmissionStat> {
        val submissionStats = stats.associateBy({ it.type.name }, { it.value })
        return statsDataRepository
            .upsertStats(accNo, submissionStats)
            .stats
            .map { SingleSubmissionStat(accNo = accNo, type = SubmissionStatType.fromString(it.key), value = it.value) }
    }

    override suspend fun saveAll(stats: List<SubmissionStat>): BulkWriteResult {
        val upserts =
            stats
                .map {
                    UpdateOneModel<Document>(
                        Filters.eq("accNo", it.accNo),
                        Updates.set("$SUB_STATS.${it.type}", it.value),
                        UpdateOptions().upsert(true),
                    )
                }
        return statsDataRepository.bulkWrite(upserts)
    }

    override suspend fun incrementAll(stats: List<SubmissionStat>): BulkWriteResult {
        val upserts =
            stats.map {
                UpdateOneModel<Document>(
                    Filters.eq("accNo", it.accNo),
                    Updates.inc("$SUB_STATS.${it.type}", it.value),
                    UpdateOptions().upsert(true),
                )
            }
        return statsDataRepository.bulkWrite(upserts)
    }

    private fun toSubmissionStat(
        type: SubmissionStatType,
        docSubmission: DocSubmissionStats,
    ): SingleSubmissionStat = SingleSubmissionStat(docSubmission.accNo, docSubmission.stats[type.name]!!, type)
}
