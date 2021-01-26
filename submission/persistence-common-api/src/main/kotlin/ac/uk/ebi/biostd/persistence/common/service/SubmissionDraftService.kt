package ac.uk.ebi.biostd.persistence.common.service

import ac.uk.ebi.biostd.persistence.common.request.PaginationFilter
import ebi.ac.uk.model.SubmissionDraft

interface SubmissionDraftService {

    fun getSubmissionDraft(userId: Long, key: String): SubmissionDraft

    fun updateSubmissionDraft(userId: Long, key: String, content: String): SubmissionDraft

    fun deleteSubmissionDraft(userId: Long, key: String)

    fun getSubmissionsDraft(userId: Long, filter: PaginationFilter = PaginationFilter()): List<SubmissionDraft>

    fun createSubmissionDraft(userId: Long, content: String): SubmissionDraft
}
