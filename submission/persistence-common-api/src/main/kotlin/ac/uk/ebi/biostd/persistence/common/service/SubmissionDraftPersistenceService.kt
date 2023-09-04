package ac.uk.ebi.biostd.persistence.common.service

import ac.uk.ebi.biostd.persistence.common.model.SubmissionDraft
import ac.uk.ebi.biostd.persistence.common.request.PaginationFilter
import kotlinx.coroutines.flow.Flow

interface SubmissionDraftPersistenceService {
    suspend fun findSubmissionDraft(userEmail: String, key: String): SubmissionDraft?

    suspend fun updateSubmissionDraft(userEmail: String, key: String, content: String): SubmissionDraft

    suspend fun setAcceptedStatus(key: String)

    suspend fun setActiveStatus(key: String)

    suspend fun deleteSubmissionDraft(userEmail: String, key: String)

    fun getActiveSubmissionDrafts(
        userEmail: String,
        filter: PaginationFilter = PaginationFilter(),
    ): Flow<SubmissionDraft>

    suspend fun createSubmissionDraft(userEmail: String, key: String, content: String): SubmissionDraft

    suspend fun setProcessingStatus(userEmail: String, key: String)
}
