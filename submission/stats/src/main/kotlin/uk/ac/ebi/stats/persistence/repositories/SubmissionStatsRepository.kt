package uk.ac.ebi.stats.persistence.repositories

import org.springframework.data.jpa.repository.JpaRepository
import uk.ac.ebi.stats.model.SubmissionStatType
import uk.ac.ebi.stats.persistence.model.SubmissionStatDb

interface SubmissionStatsRepository : JpaRepository<SubmissionStatDb, Long> {
    fun existsByAccNoAndType(accNo: String, type: SubmissionStatType): Boolean

    fun findAllByType(type: SubmissionStatType): List<SubmissionStatDb>

    fun findByAccNoAndType(accNo: String, type: SubmissionStatType): SubmissionStatDb?
}
