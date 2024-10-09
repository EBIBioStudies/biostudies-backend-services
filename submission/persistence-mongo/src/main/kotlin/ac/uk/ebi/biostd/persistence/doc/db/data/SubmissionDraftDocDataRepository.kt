package ac.uk.ebi.biostd.persistence.doc.db.data

import ac.uk.ebi.biostd.persistence.common.request.PageRequest
import ac.uk.ebi.biostd.persistence.doc.commons.replaceOrCreate
import ac.uk.ebi.biostd.persistence.doc.db.reactive.repositories.SubmissionDraftRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft.Companion.CONTENT
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft.Companion.KEY
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft.Companion.OWNER
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft.Companion.STATUS
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft.DraftStatus
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft.DraftStatus.ACCEPTED
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft.DraftStatus.ACTIVE
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update.update

class SubmissionDraftDocDataRepository(
    private val submissionDraftRepository: SubmissionDraftRepository,
    private val mongoTemplate: ReactiveMongoTemplate,
) : SubmissionDraftRepository by submissionDraftRepository {
    suspend fun saveDraft(
        owner: String,
        key: String,
        content: String,
    ): DocSubmissionDraft {
        val draft = DocSubmissionDraft(owner, key, content, ACTIVE)
        return mongoTemplate.replaceOrCreate(
            Query(where(OWNER).`is`(owner).andOperator(where(KEY).`is`(key), where(STATUS).`is`(ACTIVE))),
            draft,
        )
    }

    suspend fun setStatus(
        owner: String,
        key: String,
        newStatus: DraftStatus,
    ) {
        val query =
            Query(
                where(OWNER).`is`(owner).andOperator(where(KEY).`is`(key), where(STATUS).ne(ACCEPTED)),
            )
        mongoTemplate.updateFirst(query, update(STATUS, newStatus), DocSubmissionDraft::class.java).awaitSingle()
    }

    suspend fun setStatus(
        key: String,
        status: DraftStatus,
    ) {
        val query = Query(where(KEY).`is`(key).andOperator(where(STATUS).ne(ACCEPTED)))
        mongoTemplate.updateMulti(query, update(STATUS, status), DocSubmissionDraft::class.java).awaitSingle()
    }

    suspend fun updateDraftContent(
        owner: String,
        key: String,
        content: String,
    ) {
        mongoTemplate.updateFirst(
            Query(where(OWNER).`is`(owner).andOperator(where(KEY).`is`(key), where(STATUS).`is`(ACTIVE))),
            update(CONTENT, content),
            DocSubmissionDraft::class.java,
        ).awaitSingle()
    }

    suspend fun createDraft(
        owner: String,
        key: String,
        content: String,
    ): DocSubmissionDraft {
        val draft = DocSubmissionDraft(owner, key, content, ACTIVE)
        return submissionDraftRepository.save(draft)
    }

    fun findAllByOwnerAndStatus(
        owner: String,
        status: DraftStatus,
        pageRequest: PageRequest = PageRequest(),
    ): Flow<DocSubmissionDraft> {
        val request = pageRequest.asDataPageRequest()
        return submissionDraftRepository.findAllByOwnerAndStatus(owner, status, request)
    }
}
