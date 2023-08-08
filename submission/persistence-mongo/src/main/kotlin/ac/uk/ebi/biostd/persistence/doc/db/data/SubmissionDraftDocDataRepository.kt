package ac.uk.ebi.biostd.persistence.doc.db.data

import ac.uk.ebi.biostd.persistence.common.request.PaginationFilter
import ac.uk.ebi.biostd.persistence.doc.commons.replaceOrCreate
import ac.uk.ebi.biostd.persistence.doc.db.repositories.SubmissionDraftRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft.Companion.CONTENT
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft.Companion.KEY
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft.Companion.STATUS
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft.Companion.USER_ID
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft.DraftStatus
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft.DraftStatus.ACCEPTED
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft.DraftStatus.ACTIVE
import org.springframework.data.domain.PageRequest
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update.update

class SubmissionDraftDocDataRepository(
    private val submissionDraftRepository: SubmissionDraftRepository,
    private val mongoTemplate: ReactiveMongoTemplate,
) : SubmissionDraftRepository by submissionDraftRepository {
    fun saveDraft(userId: String, key: String, content: String): DocSubmissionDraft {
        val draft = DocSubmissionDraft(userId, key, content, ACTIVE)
        return mongoTemplate.replaceOrCreate(
            Query(where(USER_ID).`is`(userId).andOperator(where(KEY).`is`(key), where(STATUS).`is`(ACTIVE))),
            draft
        )
    }

    fun setStatus(userEmail: String, key: String, newStatus: DraftStatus) {
        val query = Query(
            where(USER_ID).`is`(userEmail).andOperator(where(KEY).`is`(key), where(STATUS).ne(ACCEPTED))
        )
        mongoTemplate.updateFirst(query, update(STATUS, newStatus), DocSubmissionDraft::class.java).block()
    }

    fun setStatus(key: String, status: DraftStatus) {
        val query = Query(where(KEY).`is`(key).andOperator(where(STATUS).ne(ACCEPTED)))
        mongoTemplate.updateMulti(query, update(STATUS, status), DocSubmissionDraft::class.java).block()
    }

    fun updateDraftContent(userId: String, key: String, content: String) {
        mongoTemplate.updateFirst(
            Query(where(USER_ID).`is`(userId).andOperator(where(KEY).`is`(key), where(STATUS).`is`(ACTIVE))),
            update(CONTENT, content),
            DocSubmissionDraft::class.java
        ).block()
    }

    fun createDraft(userId: String, key: String, content: String): DocSubmissionDraft {
        val draft = DocSubmissionDraft(userId, key, content, ACTIVE)
        return submissionDraftRepository.save(draft)
    }

    fun findAllByUserIdAndStatus(
        userId: String,
        status: DraftStatus,
        filter: PaginationFilter = PaginationFilter(),
    ): List<DocSubmissionDraft> {
        val pageRequest = PageRequest.of(filter.pageNumber, filter.limit)
        return submissionDraftRepository.findAllByUserIdAndStatus(userId, status, pageRequest)
    }
}
