package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.model.SubmissionDraft
import ac.uk.ebi.biostd.persistence.common.request.PageRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionDraftPersistenceService
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDraftDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft.DraftStatus.ACCEPTED
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft.DraftStatus.ACTIVE
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft.DraftStatus.PROCESSING
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SubmissionDraftMongoPersistenceService(
    private val draftDocDataRepository: SubmissionDraftDocDataRepository,
) : SubmissionDraftPersistenceService {
    override suspend fun findSubmissionDraft(
        userEmail: String,
        key: String,
    ): SubmissionDraft? {
        return draftDocDataRepository
            .findByUserIdAndKeyAndStatusIsNot(userEmail, key, ACCEPTED)
            ?.let { SubmissionDraft(it.key, it.content) }
    }

    override suspend fun updateSubmissionDraft(
        userEmail: String,
        key: String,
        content: String,
    ): SubmissionDraft {
        draftDocDataRepository.updateDraftContent(userEmail, key, content)
        return SubmissionDraft(key, content)
    }

    override suspend fun setAcceptedStatus(key: String) {
        draftDocDataRepository.setStatus(key, ACCEPTED)
    }

    override suspend fun setActiveStatus(key: String) {
        draftDocDataRepository.setStatus(key, ACTIVE)
    }

    override suspend fun deleteSubmissionDraft(
        userEmail: String,
        key: String,
    ) {
        draftDocDataRepository.deleteByUserIdAndKey(userEmail, key)
    }

    override fun getActiveSubmissionDrafts(
        userEmail: String,
        filter: PageRequest,
    ): Flow<SubmissionDraft> {
        return draftDocDataRepository
            .findAllByUserIdAndStatus(userEmail, ACTIVE, filter)
            .map { SubmissionDraft(it.key, it.content) }
    }

    override suspend fun createSubmissionDraft(
        userEmail: String,
        key: String,
        content: String,
    ): SubmissionDraft {
        val draft = draftDocDataRepository.createDraft(userEmail, key, content)
        return SubmissionDraft(draft.key, draft.content)
    }

    override suspend fun setProcessingStatus(
        userEmail: String,
        key: String,
    ) = draftDocDataRepository.setStatus(userEmail, key, PROCESSING)
}
