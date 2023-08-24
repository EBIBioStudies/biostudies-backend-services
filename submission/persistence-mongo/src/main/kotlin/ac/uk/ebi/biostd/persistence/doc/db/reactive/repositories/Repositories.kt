package ac.uk.ebi.biostd.persistence.doc.db.reactive.repositories

import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionStats
import org.bson.types.ObjectId
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux

interface SubmissionDraftRepository : ReactiveCrudRepository<DocSubmissionDraft, String> {
    suspend fun findByUserIdAndKeyAndStatusIsNot(
        userId: String,
        key: String,
        deleted: DocSubmissionDraft.DraftStatus,
    ): DocSubmissionDraft?

    fun findAllByUserIdAndStatus(
        userId: String,
        status: DocSubmissionDraft.DraftStatus,
        pageRequest: Pageable,
    ): Flux<DocSubmissionDraft>

    suspend fun getById(id: String): DocSubmissionDraft

    suspend fun deleteByUserIdAndKey(userId: String, draftKey: String)
}

interface SubmissionStatsRepository : ReactiveCrudRepository<DocSubmissionStats, ObjectId> {
    suspend fun getByAccNo(accNo: String): DocSubmissionStats

    suspend fun findByAccNo(accNo: String): DocSubmissionStats?

    @Query("{ 'accNo': '?0', 'stats.?1': { \$exists: true } }")
    suspend fun findByAccNoAndStatType(accNo: String, statType: SubmissionStatType): DocSubmissionStats?

    @Query("{ 'stats.?0': { \$exists: true } }")
    fun findAllByStatType(statType: SubmissionStatType, pageable: Pageable): Flux<DocSubmissionStats>
}
