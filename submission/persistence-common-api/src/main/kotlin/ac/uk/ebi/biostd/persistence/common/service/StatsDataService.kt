package ac.uk.ebi.biostd.persistence.common.service

import ac.uk.ebi.biostd.persistence.common.model.SubmissionStat
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStats
import ac.uk.ebi.biostd.persistence.common.request.PageRequest
import com.mongodb.bulk.BulkWriteResult
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Suppress("TooManyFunctions")
interface StatsDataService {
    fun findAll(lastUpdated: Instant): Flow<String>

    suspend fun findByAccNo(accNo: String): SubmissionStats?

    suspend fun findStatsByAccNo(accNo: String): List<SubmissionStat>

    fun findStatsByType(
        submissionStatType: SubmissionStatType,
        filter: PageRequest,
    ): Flow<SubmissionStat>

    suspend fun findStatByAccNoAndType(
        accNo: String,
        submissionStatType: SubmissionStatType,
    ): SubmissionStat

    suspend fun saveStat(stat: SubmissionStat): SubmissionStat

    suspend fun incrementAll(stats: List<SubmissionStat>): BulkWriteResult

    /**
     * Save all the given stats that may bellow to a different submission.
     */
    suspend fun saveAll(stats: List<SubmissionStat>): BulkWriteResult

    /**
     * Save all the given stats and update submission latest updated date.
     */
    suspend fun saveAll(
        accNo: String,
        collections: List<String>,
        stats: List<SubmissionStat>,
    ): BulkWriteResult

    suspend fun lastUpdated(accNo: String): Instant?

    suspend fun deleteStatsByAccNo(accNo: String)
}
