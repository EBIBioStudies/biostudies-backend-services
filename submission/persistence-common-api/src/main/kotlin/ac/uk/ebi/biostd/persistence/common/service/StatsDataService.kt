package ac.uk.ebi.biostd.persistence.common.service

import ac.uk.ebi.biostd.persistence.common.model.SubmissionStat
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType
import ac.uk.ebi.biostd.persistence.common.request.PaginationFilter
import kotlinx.coroutines.flow.Flow

interface StatsDataService {
    suspend fun findByAccNo(accNo: String): List<SubmissionStat>

    fun findByType(submissionStatType: SubmissionStatType, filter: PaginationFilter): Flow<SubmissionStat>

    suspend fun findByAccNoAndType(accNo: String, submissionStatType: SubmissionStatType): SubmissionStat

    suspend fun save(stat: SubmissionStat): SubmissionStat

    suspend fun saveAll(stats: List<SubmissionStat>): List<SubmissionStat>

    suspend fun incrementAll(stats: List<SubmissionStat>): List<SubmissionStat>
}
