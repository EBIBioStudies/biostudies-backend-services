package ac.uk.ebi.biostd.persistence.common.service

import ac.uk.ebi.biostd.persistence.common.model.SubmissionStat
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType
import ac.uk.ebi.biostd.persistence.common.request.PaginationFilter

interface StatsDataService {
    fun findByType(submissionStatType: SubmissionStatType, filter: PaginationFilter): List<SubmissionStat>

    fun findByAccNoAndType(accNo: String, submissionStatType: SubmissionStatType): SubmissionStat

    fun saveAll(stats: List<SubmissionStat>): List<SubmissionStat>

    fun incrementAll(stats: List<SubmissionStat>): List<SubmissionStat>
}
