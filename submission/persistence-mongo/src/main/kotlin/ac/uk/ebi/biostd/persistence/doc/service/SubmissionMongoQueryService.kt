package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.model.BasicSubmission
import ac.uk.ebi.biostd.persistence.common.request.SubmissionFilter
import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.mapping.to.ToExtSubmissionMapper
import ac.uk.ebi.biostd.persistence.doc.model.asBasicSubmission
import ebi.ac.uk.extended.model.ExtSubmission
import org.springframework.data.domain.Page

internal class SubmissionMongoQueryService(
    private val submissionRepo: SubmissionDocDataRepository,
    private val requestRepository: SubmissionRequestDocDataRepository,
    private val toExtSubmissionMapper: ToExtSubmissionMapper
) : SubmissionQueryService {
    override fun existByAccNo(accNo: String): Boolean = submissionRepo.existsByAccNo(accNo)

    override fun getExtByAccNo(accNo: String): ExtSubmission {
        val document = submissionRepo.getByAccNoAndVersionGreaterThan(accNo, 0)
        return toExtSubmissionMapper.toExtSubmission(document)
    }

    override fun getExtByAccNoAndVersion(accNo: String, version: Int): ExtSubmission {
        val document = submissionRepo.getByAccNoAndVersion(accNo, version)
        return toExtSubmissionMapper.toExtSubmission(document)
    }

    override fun expireSubmission(accNo: String) {
        val newVersion = submissionRepo.getByAccNoAndVersionGreaterThan(accNo, 0)
        submissionRepo.save(newVersion.copy(version = -newVersion.version))
    }

    override fun getExtendedSubmissions(filter: SubmissionFilter): Page<Result<ExtSubmission>> {
        return submissionRepo.getSubmissionsPage(filter)
            .map { runCatching { toExtSubmissionMapper.toExtSubmission(it) } }
    }

    override fun getSubmissionsByUser(email: String, filter: SubmissionFilter): List<BasicSubmission> {
        return submissionRepo.getSubmissions(filter, email).map { it.asBasicSubmission() }
    }

    override fun getRequest(accNo: String, version: Int): ExtSubmission {
        val submission = requestRepository.getByAccNoAndVersion(accNo, version).submission
        return toExtSubmissionMapper.toExtSubmission(submission)
    }
}
