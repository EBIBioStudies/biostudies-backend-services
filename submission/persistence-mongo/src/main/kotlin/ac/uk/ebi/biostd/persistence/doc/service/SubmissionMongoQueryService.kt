package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.model.BasicSubmission
import ac.uk.ebi.biostd.persistence.common.request.SubmissionFilter
import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.mapping.to.ToExtSubmissionMapper
import ebi.ac.uk.extended.model.ExtSubmission
import org.springframework.data.domain.Page

internal class SubmissionMongoQueryService(
    private val submissionRepo: SubmissionDocDataRepository,
    private val toExtSubmissionMapper: ToExtSubmissionMapper
) : SubmissionQueryService {
    override fun existByAccNo(accNo: String): Boolean = submissionRepo.existsByAccNo(accNo)

    override fun getExtByAccNo(accNo: String): ExtSubmission {
        val document = submissionRepo.getByAccNo(accNo)
        return toExtSubmissionMapper.toExtSubmission(document)
    }

    override fun getExtByAccNoAndVersion(accNo: String, version: Int): ExtSubmission {
        val document = submissionRepo.getByAccNoAndVersion(accNo, version)
        return toExtSubmissionMapper.toExtSubmission(document)
    }

    override fun expireSubmission(accNo: String) {
        val actives = submissionRepo.getByAccNoAndVersionGreaterThan(accNo, 0).map { it.copy(version = -it.version) }
        submissionRepo.saveAll(actives)
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
