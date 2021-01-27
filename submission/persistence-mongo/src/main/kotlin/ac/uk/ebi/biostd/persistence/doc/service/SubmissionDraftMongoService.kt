package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.request.PaginationFilter
import ac.uk.ebi.biostd.persistence.common.service.SubmissionDraftService
import ebi.ac.uk.model.SubmissionDraft

class SubmissionDraftMongoService : SubmissionDraftService {

    override fun getSubmissionDraft(userId: Long, key: String): SubmissionDraft {
        TODO("Not yet implemented")
    }

    override fun updateSubmissionDraft(userId: Long, key: String, content: String): SubmissionDraft {
        TODO("Not yet implemented")
    }

    override fun deleteSubmissionDraft(userId: Long, key: String) {
        TODO("Not yet implemented")
    }

    override fun getSubmissionsDraft(userId: Long, filter: PaginationFilter): List<SubmissionDraft> {
        TODO("Not yet implemented")
    }

    override fun createSubmissionDraft(userId: Long, content: String): SubmissionDraft {
        TODO("Not yet implemented")
    }
}
