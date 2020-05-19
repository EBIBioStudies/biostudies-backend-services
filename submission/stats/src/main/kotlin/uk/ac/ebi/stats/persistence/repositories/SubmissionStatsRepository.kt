package uk.ac.ebi.stats.persistence.repositories

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.PagingAndSortingRepository
import uk.ac.ebi.stats.model.SubmissionStatType
import uk.ac.ebi.stats.persistence.model.SubmissionStatDb

interface SubmissionStatsRepository : PagingAndSortingRepository<SubmissionStatDb, Long> {
    fun existsByAccNoAndType(accNo: String, type: SubmissionStatType): Boolean

    fun findAllByType(type: SubmissionStatType, pageable: Pageable): Page<SubmissionStatDb>

    fun findByAccNoAndType(accNo: String, type: SubmissionStatType): SubmissionStatDb?

    fun getByAccNoAndType(accNo: String, type: SubmissionStatType): SubmissionStatDb
}
