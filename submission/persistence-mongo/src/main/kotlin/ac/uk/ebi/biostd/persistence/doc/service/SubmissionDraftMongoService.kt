package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat.JsonFormat.JsonPretty
import ac.uk.ebi.biostd.persistence.common.model.SubmissionDraft
import ac.uk.ebi.biostd.persistence.common.request.PaginationFilter
import ac.uk.ebi.biostd.persistence.common.service.SubmissionDraftService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDraftDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft.DraftStatus.ACTIVE
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft.DraftStatus.PROCESSING
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
import java.time.Instant

class SubmissionDraftMongoService(
    private val draftDocDataRepository: SubmissionDraftDocDataRepository,
    private val submissionQueryService: SubmissionQueryService,
    private val serializationService: SerializationService,
    private val toSubmissionMapper: ToSubmissionMapper,
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

    override fun getActiveSubmissionsDraft(userEmail: String, filter: PaginationFilter): List<SubmissionDraft> {
        return draftDocDataRepository
            .findAllByUserIdAndStatus(userEmail, ACTIVE, filter)
            .map { SubmissionDraft(it.key, it.content) }
    }

    override fun createSubmissionDraft(userEmail: String, content: String): SubmissionDraft {
        val draft = draftDocDataRepository.createDraft(userEmail, "TMP_${Instant.now().toEpochMilli()}", content)
        return SubmissionDraft(draft.key, draft.content)
    }

    override fun setProcessingStatus(userEmail: String, key: String) =
        draftDocDataRepository.setStatus(userEmail, key, PROCESSING)

    private fun create(userEmail: String, key: String): DocSubmissionDraft {
        val submission = toSubmissionMapper.toSimpleSubmission(submissionQueryService.getExtByAccNo(key))
        val content = serializationService.serializeSubmission(submission, JsonPretty)
        return draftDocDataRepository.createDraft(userEmail, key, content)
    }
}
