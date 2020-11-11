package ac.uk.ebi.biostd.persistence.common.service

import ac.uk.ebi.biostd.persistence.common.model.SubmissionStat
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface SubmissionStatsDataService {
    fun findByAccNo(accNo: String): List<SubmissionStat>
    fun existsByAccNoAndType(accNo: String, type: SubmissionStatType): Boolean
    fun findAllByType(type: SubmissionStatType, pageable: Pageable): Page<SubmissionStat>
    fun findByAccNoAndType(accNo: String, type: SubmissionStatType): SubmissionStat?
    fun getByAccNoAndType(accNo: String, type: SubmissionStatType): SubmissionStat
}
