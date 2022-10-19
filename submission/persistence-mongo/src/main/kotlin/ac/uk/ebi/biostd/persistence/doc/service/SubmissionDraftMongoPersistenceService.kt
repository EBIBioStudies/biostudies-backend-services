package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.model.SubmissionDraft
import ac.uk.ebi.biostd.persistence.common.request.PaginationFilter
import ac.uk.ebi.biostd.persistence.common.service.SubmissionDraftPersistenceService
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDraftDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft.DraftStatus.ACTIVE
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft.DraftStatus.DELETED
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft.DraftStatus.PROCESSING

class SubmissionDraftMongoPersistenceService(
    private val draftDocDataRepository: SubmissionDraftDocDataRepository,
) : SubmissionDraftPersistenceService {
    override fun findSubmissionDraft(userEmail: String, key: String): SubmissionDraft? {
        return draftDocDataRepository
            .findByUserIdAndKeyAndStatusIsNot(userEmail, key, DELETED)
            ?.let { SubmissionDraft(it.key, it.content) }
    }

    override fun updateSubmissionDraft(userEmail: String, key: String, content: String): SubmissionDraft {
        draftDocDataRepository.updateDraftContent(userEmail, key, content)
        return SubmissionDraft(key, content)
    }

    override fun setDeleteStatus(key: String) {
        draftDocDataRepository.setStatus(key, DELETED)
    }

    override fun setActiveStatus(key: String) {
        draftDocDataRepository.setStatus(key, ACTIVE)
    }

    override fun deleteSubmissionDraft(userEmail: String, key: String) {
        draftDocDataRepository.deleteByUserIdAndKey(userEmail, key)
    }

    override fun getActiveSubmissionDrafts(userEmail: String, filter: PaginationFilter): List<SubmissionDraft> {
        return draftDocDataRepository
            .findAllByUserIdAndStatus(userEmail, ACTIVE, filter)
            .map { SubmissionDraft(it.key, it.content) }
    }

    override fun createSubmissionDraft(userEmail: String, key: String, content: String): SubmissionDraft {
        val draft = draftDocDataRepository.createDraft(userEmail, key, content)
        return SubmissionDraft(draft.key, draft.content)
    }

    override fun setActiveStatus(
        userEmail: String,
        key: String,
    ) = draftDocDataRepository.setStatus(userEmail, key, ACTIVE)

    override fun setProcessingStatus(
        userEmail: String,
        key: String,
    ) = draftDocDataRepository.setStatus(userEmail, key, PROCESSING)
}
