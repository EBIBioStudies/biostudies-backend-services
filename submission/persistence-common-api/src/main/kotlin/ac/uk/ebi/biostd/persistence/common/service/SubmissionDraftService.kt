package ac.uk.ebi.biostd.persistence.common.service

import ac.uk.ebi.biostd.persistence.common.request.PaginationFilter
import ebi.ac.uk.model.SubmissionDraft

interface SubmissionDraftService {

    fun getSubmissionDraft(userEmail: String, key: String): SubmissionDraft

    fun updateSubmissionDraft(userEmail: String, key: String, content: String): SubmissionDraft

    fun deleteSubmissionDraft(userEmail: String, key: String)

    fun getSubmissionsDraft(userEmail: String, filter: PaginationFilter = PaginationFilter()): List<SubmissionDraft>

    fun createSubmissionDraft(userEmail: String, content: String): SubmissionDraft
}
