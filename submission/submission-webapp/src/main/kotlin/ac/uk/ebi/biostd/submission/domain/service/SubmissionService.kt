package ac.uk.ebi.biostd.submission.domain.service

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat.JsonFormat.JsonPretty
import ac.uk.ebi.biostd.integration.SubFormat.TsvFormat.Tsv
import ac.uk.ebi.biostd.integration.SubFormat.XmlFormat
import ac.uk.ebi.biostd.persistence.filter.SubmissionFilter
import ac.uk.ebi.biostd.persistence.integration.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.projections.SimpleSubmission
import ac.uk.ebi.biostd.persistence.service.SubmissionRepository
import ac.uk.ebi.biostd.submission.model.SubmissionRequest
import ac.uk.ebi.biostd.submission.submitter.SubmissionPersistenceService
import ac.uk.ebi.biostd.submission.submitter.SubmissionSubmitter
import ebi.ac.uk.model.Submission
import ebi.ac.uk.paths.FILES_PATH
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import ebi.ac.uk.security.integration.model.api.SecurityUser

// TODO: merge with QueryService to provide operations.
class SubmissionService(
    private val subRepository: SubmissionRepository,
    private val submissionPersistenceService: SubmissionPersistenceService,
    private val serializationService: SerializationService,
    private val userPrivilegesService: IUserPrivilegesService,
    private val queryService: SubmissionQueryService,
    private val submissionSubmitter: SubmissionSubmitter
) {
    fun submit(request: SubmissionRequest): Submission = submissionSubmitter.submit(request)

    fun getSubmissionAsJson(accNo: String): String {
        val submission = subRepository.getByAccNo(accNo)
        return serializationService.serializeSubmission(submission, JsonPretty)
    }

    fun getSubmissionAsXml(accNo: String): String {
        val submission = subRepository.getByAccNo(accNo)
        return serializationService.serializeSubmission(submission, XmlFormat)
    }

    fun getSubmissionAsTsv(accNo: String): String {
        val submission = subRepository.getByAccNo(accNo)
        return serializationService.serializeSubmission(submission, Tsv)
    }

    fun getSubmissions(user: SecurityUser, filter: SubmissionFilter): List<SimpleSubmission> =
        subRepository.getSubmissionsByUser(user.id, filter)

    fun deleteSubmission(accNo: String, user: SecurityUser) {
        require(userPrivilegesService.canDelete(user.email, accNo))
        subRepository.expireSubmission(accNo)
    }

    fun submissionFolder(accNo: String): java.io.File? = queryService.getExistingFolder(accNo)?.resolve(FILES_PATH)

    fun refreshSubmission(user: SecurityUser, accNo: String) {
        require(userPrivilegesService.canResubmit(user.email, accNo))
        submissionPersistenceService.refresh(accNo)
    }
}
