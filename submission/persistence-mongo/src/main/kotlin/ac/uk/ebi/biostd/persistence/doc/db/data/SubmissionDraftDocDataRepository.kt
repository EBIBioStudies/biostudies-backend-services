package ac.uk.ebi.biostd.persistence.doc.db.data

import ac.uk.ebi.biostd.persistence.common.request.PaginationFilter
import ac.uk.ebi.biostd.persistence.doc.commons.replaceOrCreate
import ac.uk.ebi.biostd.persistence.doc.db.repositories.SubmissionDraftRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft.Companion.KEY
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft.Companion.USER_ID
import org.springframework.data.domain.PageRequest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query

class SubmissionDraftDocDataRepository(
    private val submissionDraftRepository: SubmissionDraftRepository,
    private val mongoTemplate: MongoTemplate
) : SubmissionDraftRepository by submissionDraftRepository {

    fun saveSubmissionDraft(userId: Long, key: String, content: String): DocSubmissionDraft =
        mongoTemplate.replaceOrCreate(
            Query(where(USER_ID).`is`(userId).andOperator(where(KEY).`is`(key))),
            DocSubmissionDraft(userId, key, content)
        )

    fun createSubmissionDraft(userId: Long, key: String, content: String): DocSubmissionDraft =
        submissionDraftRepository.save(DocSubmissionDraft(userId, key, content))

    fun findAllByUserId(userId: Long, filter: PaginationFilter = PaginationFilter()): List<DocSubmissionDraft> =
        submissionDraftRepository.findAllByUserId(userId, PageRequest.of(filter.pageNumber, filter.limit))
}
