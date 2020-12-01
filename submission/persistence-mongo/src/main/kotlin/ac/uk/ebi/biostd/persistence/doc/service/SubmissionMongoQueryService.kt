package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.model.BasicSubmission
import ac.uk.ebi.biostd.persistence.common.request.SubmissionFilter
import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.doc.db.repositories.SubmissionMongoRepository
import ebi.ac.uk.extended.model.ExtSubmission
import org.springframework.data.domain.Page

class SubmissionMongoQueryService(
    private val submissionRepository: SubmissionMongoRepository
) : SubmissionQueryService {
    override fun existByAccNo(accNo: String): Boolean = submissionRepository.existsByAccNo(accNo)

    override fun getExtByAccNo(accNo: String): ExtSubmission {
        TODO("Not yet implemented")
    }

    override fun getExtByAccNoAndVersion(accNo: String, version: Int): ExtSubmission {
        TODO("Not yet implemented")
    }

    override fun expireSubmission(accNo: String) {
        TODO("Not yet implemented")
    }

    override fun getExtendedSubmissions(filter: SubmissionFilter, offset: Long, limit: Int): Page<ExtSubmission> {
        TODO("Not yet implemented")
    }

    override fun getSubmissionsByUser(userId: Long, filter: SubmissionFilter): List<BasicSubmission> {
        TODO("Not yet implemented")
    }

    override fun getRequest(accNo: String, version: Int): ExtSubmission {
        TODO("Not yet implemented")
    }
}
