package ac.uk.ebi.biostd.persistence.doc.db.reactive.repositories

import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft
import org.springframework.data.domain.Pageable
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
