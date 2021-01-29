package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.request.PaginationFilter
import ac.uk.ebi.biostd.persistence.common.service.SubmissionDraftService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDraftDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft
import ebi.ac.uk.model.SubmissionDraft
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import java.time.Instant

class SubmissionDraftMongoService(
    private val draftDocDataRepository: SubmissionDraftDocDataRepository,
    private val submissionQueryService: SubmissionQueryService,
    private val extSerializationService: ExtSerializationService
) : SubmissionDraftService {

    override fun getSubmissionDraft(userId: Long, key: String): SubmissionDraft {
        val draft = draftDocDataRepository.findByUserIdAndKey(userId, key) ?: create(userId, key)
        return SubmissionDraft(draft.key, draft.content)
    }

    override fun updateSubmissionDraft(userId: Long, key: String, content: String): SubmissionDraft {
        val draft = draftDocDataRepository.saveSubmissionDraft(userId, key, content)
        return SubmissionDraft(draft.key, draft.content)
    }

    override fun deleteSubmissionDraft(userId: Long, key: String) =
        draftDocDataRepository.deleteByUserIdAndKey(userId, key)

    override fun getSubmissionsDraft(userId: Long, filter: PaginationFilter): List<SubmissionDraft> =
        draftDocDataRepository.findAllByUserId(userId, filter).map { SubmissionDraft(it.key, it.content) }

    override fun createSubmissionDraft(userId: Long, content: String): SubmissionDraft {
        val draft = draftDocDataRepository.createSubmissionDraft(userId, "TMP_${Instant.now().toEpochMilli()}", content)
        return SubmissionDraft(draft.key, draft.content)
    }

    private fun create(userId: Long, key: String): DocSubmissionDraft {
        val submission = submissionQueryService.getExtByAccNo(key)
        return DocSubmissionDraft(userId, key, extSerializationService.serialize(submission))
    }
}
