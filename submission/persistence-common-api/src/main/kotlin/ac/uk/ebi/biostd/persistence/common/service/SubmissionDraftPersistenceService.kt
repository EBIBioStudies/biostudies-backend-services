package ac.uk.ebi.biostd.persistence.common.service

import ac.uk.ebi.biostd.persistence.common.model.SubmissionDraft
import ac.uk.ebi.biostd.persistence.common.request.PaginationFilter

interface SubmissionDraftPersistenceService {
    fun findSubmissionDraft(userEmail: String, key: String): SubmissionDraft?

    fun updateSubmissionDraft(userEmail: String, key: String, content: String): SubmissionDraft

    fun setDeleteStatus(key: String)

    fun setActiveStatus(key: String)

    fun deleteSubmissionDraft(userEmail: String, key: String)

    fun getActiveSubmissionDrafts(
        userEmail: String,
        filter: PaginationFilter = PaginationFilter(),
    ): List<SubmissionDraft>

    fun createSubmissionDraft(userEmail: String, key: String, content: String): SubmissionDraft

    fun setProcessingStatus(userEmail: String, key: String)
}
