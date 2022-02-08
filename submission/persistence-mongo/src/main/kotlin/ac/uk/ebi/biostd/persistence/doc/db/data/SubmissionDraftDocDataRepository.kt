package ac.uk.ebi.biostd.persistence.doc.db.data

import ac.uk.ebi.biostd.persistence.common.request.PaginationFilter
import ac.uk.ebi.biostd.persistence.doc.commons.replaceOrCreate
import ac.uk.ebi.biostd.persistence.doc.db.repositories.SubmissionDraftRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft.Companion.CONTENT
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft.Companion.KEY
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft.Companion.STATUS_DRAFT
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft.Companion.USER_ID
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft.DraftStatus
import org.springframework.data.domain.PageRequest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update.update

class SubmissionDraftDocDataRepository(
    private val submissionDraftRepository: SubmissionDraftRepository,
    private val mongoTemplate: MongoTemplate
) : SubmissionDraftRepository by submissionDraftRepository {
    fun saveDraft(userId: String, key: String, content: String): DocSubmissionDraft =
        mongoTemplate.replaceOrCreate(
            Query(where(USER_ID).`is`(userId).andOperator(where(KEY).`is`(key))),
            DocSubmissionDraft(userId, key, content, DraftStatus.ACTIVE)
        )

    fun setStatus(userEmail: String, key: String, status: DraftStatus) {
        val query = Query(where(USER_ID).`is`(userEmail).andOperator(where(KEY).`is`(key)))
        mongoTemplate.updateFirst(query, update(STATUS_DRAFT, status), DocSubmissionDraft::class.java)
    }

    fun updateDraftContent(userId: String, key: String, content: String) {
        mongoTemplate.updateFirst(
            Query(where(USER_ID).`is`(userId).andOperator(where(KEY).`is`(key))),
            update(CONTENT, content),
            DocSubmissionDraft::class.java
        )
    }

    fun createDraft(userId: String, key: String, content: String): DocSubmissionDraft =
        submissionDraftRepository.save(DocSubmissionDraft(userId, key, content, DraftStatus.ACTIVE))

    fun findAllByUserIdAndStatusDraft(
        userId: String,
        status: DraftStatus,
        filter: PaginationFilter = PaginationFilter()
    ): List<DocSubmissionDraft> = submissionDraftRepository.findAllByUserIdAndStatusDraft(
        userId, status, PageRequest.of(filter.pageNumber, filter.limit)
    )
}
