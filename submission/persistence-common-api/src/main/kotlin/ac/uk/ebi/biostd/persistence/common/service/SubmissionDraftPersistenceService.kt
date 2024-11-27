package ac.uk.ebi.biostd.persistence.common.service

import ac.uk.ebi.biostd.persistence.common.model.SubmissionDraft
import ac.uk.ebi.biostd.persistence.common.request.PageRequest
import kotlinx.coroutines.flow.Flow
import java.time.Instant

interface SubmissionDraftPersistenceService {
    suspend fun findSubmissionDraft(
        userEmail: String,
        key: String,
    ): SubmissionDraft?

    suspend fun updateSubmissionDraft(
        userEmail: String,
        key: String,
        content: String,
        modificationTime: Instant,
    ): SubmissionDraft

    suspend fun setAcceptedStatus(
        key: String,
        modificationTime: Instant,
    )

    suspend fun setActiveStatus(
        key: String,
        modificationTime: Instant,
    )

    suspend fun setProcessingStatus(
        userEmail: String,
        key: String,
        modificationTime: Instant,
    )

    suspend fun deleteSubmissionDraft(
        userEmail: String,
        key: String,
    )

    fun getActiveSubmissionDrafts(
        userEmail: String,
        filter: PageRequest = PageRequest(),
    ): Flow<SubmissionDraft>

    suspend fun createSubmissionDraft(
        userEmail: String,
        key: String,
        content: String,
        modificationTime: Instant,
    ): SubmissionDraft
}
