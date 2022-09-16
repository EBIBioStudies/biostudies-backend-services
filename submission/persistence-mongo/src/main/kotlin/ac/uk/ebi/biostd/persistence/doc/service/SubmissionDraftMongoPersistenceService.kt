package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.model.SubmissionDraft
import ac.uk.ebi.biostd.persistence.common.request.PaginationFilter
import ac.uk.ebi.biostd.persistence.common.service.SubmissionDraftPersistenceService
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDraftDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft.DraftStatus.ACTIVE
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft.DraftStatus.PROCESSING

class SubmissionDraftMongoPersistenceService(
    private val draftDocDataRepository: SubmissionDraftDocDataRepository,
) : SubmissionDraftPersistenceService {
    override fun findSubmissionDraft(userEmail: String, key: String): SubmissionDraft? {
        return draftDocDataRepository.findByUserIdAndKey(userEmail, key)?.toSubmissionDraft()
    }

    override fun updateSubmissionDraft(userEmail: String, key: String, content: String): SubmissionDraft {
        draftDocDataRepository.updateDraftContent(userEmail, key, content)
        return SubmissionDraft(key, content)
    }

    override fun deleteSubmissionDraft(key: String) {
        draftDocDataRepository.deleteByKey(key)
    }

    override fun deleteSubmissionDraft(userEmail: String, key: String) {
        draftDocDataRepository.deleteByUserIdAndKey(userEmail, key)
    }

    override fun getActiveSubmissionDrafts(userEmail: String, filter: PaginationFilter): List<SubmissionDraft> {
        return draftDocDataRepository
            .findAllByUserIdAndStatus(userEmail, ACTIVE, filter)
            .map { it.toSubmissionDraft() }
    }

    override fun createSubmissionDraft(userEmail: String, key: String, content: String): SubmissionDraft {
        return draftDocDataRepository.createDraft(userEmail, key, content).toSubmissionDraft()
    }

    override fun setActiveStatus(
        userEmail: String,
        key: String
    ) = draftDocDataRepository.setStatus(userEmail, key, ACTIVE)

    override fun setProcessingStatus(
        userEmail: String,
        key: String
    ) = draftDocDataRepository.setStatus(userEmail, key, PROCESSING)

    private fun DocSubmissionDraft.toSubmissionDraft() = SubmissionDraft(key, content)
}
