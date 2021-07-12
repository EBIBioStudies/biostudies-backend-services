package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.request.PaginationFilter
import ac.uk.ebi.biostd.persistence.common.service.SubmissionDraftService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDraftDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import ac.uk.ebi.biostd.persistence.common.model.SubmissionDraft
import java.time.Instant

class SubmissionDraftMongoService(
    private val draftDocDataRepository: SubmissionDraftDocDataRepository,
    private val submissionQueryService: SubmissionQueryService,
    private val extSerializationService: ExtSerializationService
) : SubmissionDraftService {
    override fun getSubmissionDraft(userEmail: String, key: String): SubmissionDraft {
        val draft = draftDocDataRepository.findByUserIdAndKey(userEmail, key) ?: create(userEmail, key)
        return SubmissionDraft(draft.key, draft.content)
    }

    override fun updateSubmissionDraft(userEmail: String, key: String, content: String): SubmissionDraft {
        draftDocDataRepository.updateDraftContent(userEmail, key, content)
        return SubmissionDraft(key, content)
    }

    override fun deleteSubmissionDraft(userEmail: String, key: String) =
        draftDocDataRepository.deleteByUserIdAndKey(userEmail, key)

    override fun getSubmissionsDraft(userEmail: String, filter: PaginationFilter): List<SubmissionDraft> =
        draftDocDataRepository.findAllByUserId(userEmail, filter).map { SubmissionDraft(it.key, it.content) }

    override fun createSubmissionDraft(userEmail: String, content: String): SubmissionDraft {
        val draft = draftDocDataRepository.createDraft(userEmail, "TMP_${Instant.now().toEpochMilli()}", content)
        return SubmissionDraft(draft.key, draft.content)
    }

    private fun create(userEmail: String, key: String): DocSubmissionDraft {
        val submission = submissionQueryService.getExtByAccNo(key)
        val content = extSerializationService.serialize(submission)
        return draftDocDataRepository.saveDraft(userEmail, key, content)
    }
}
