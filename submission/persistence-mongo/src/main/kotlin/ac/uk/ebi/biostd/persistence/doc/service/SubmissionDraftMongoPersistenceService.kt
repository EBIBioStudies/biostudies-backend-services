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
import java.time.Instant
import java.time.ZoneOffset.UTC

class SubmissionDraftMongoPersistenceService(
    private val draftDocDataRepository: SubmissionDraftDocDataRepository,
) : SubmissionDraftPersistenceService {
    override suspend fun findSubmissionDraft(
        userEmail: String,
        key: String,
    ): SubmissionDraft? {
        return draftDocDataRepository
            .findByOwnerAndKeyAndStatusIsNot(userEmail, key, ACCEPTED)
            ?.let { SubmissionDraft(it.key, it.content, it.modificationTime.atOffset(UTC)) }
    }

    override suspend fun updateSubmissionDraft(
        userEmail: String,
        key: String,
        content: String,
        modificationTime: Instant,
    ): SubmissionDraft {
        draftDocDataRepository.updateDraftContent(userEmail, key, content, modificationTime)
        return SubmissionDraft(key, content, modificationTime.atOffset(UTC))
    }

    override suspend fun setAcceptedStatus(
        key: String,
        modificationTime: Instant,
    ) {
        draftDocDataRepository.setStatus(key, ACCEPTED, modificationTime)
    }

    override suspend fun setActiveStatus(
        key: String,
        modificationTime: Instant,
    ) {
        draftDocDataRepository.setStatus(key, ACTIVE, modificationTime)
    }

    override suspend fun deleteSubmissionDraft(
        userEmail: String,
        key: String,
    ) {
        draftDocDataRepository.deleteByOwnerAndKey(userEmail, key)
    }

    override fun getActiveSubmissionDrafts(
        userEmail: String,
        filter: PageRequest,
    ): Flow<SubmissionDraft> {
        return draftDocDataRepository
            .findAllByOwnerAndStatus(userEmail, ACTIVE, filter)
            .map { SubmissionDraft(it.key, it.content, it.modificationTime.atOffset(UTC)) }
    }

    override suspend fun createSubmissionDraft(
        userEmail: String,
        key: String,
        content: String,
        modificationTime: Instant,
    ): SubmissionDraft {
        val draft = draftDocDataRepository.createDraft(userEmail, key, content, modificationTime)
        return SubmissionDraft(draft.key, draft.content, modificationTime.atOffset(UTC))
    }

    override suspend fun setProcessingStatus(
        userEmail: String,
        key: String,
        modificationTime: Instant,
    ) = draftDocDataRepository.setStatus(userEmail, key, PROCESSING, modificationTime)
}
