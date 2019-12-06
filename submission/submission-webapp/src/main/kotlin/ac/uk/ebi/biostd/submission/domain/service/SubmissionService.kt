package ac.uk.ebi.biostd.submission.domain.service

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat.JsonFormat.JsonPretty
import ac.uk.ebi.biostd.integration.SubFormat.TsvFormat.Tsv
import ac.uk.ebi.biostd.integration.SubFormat.XmlFormat
import ac.uk.ebi.biostd.persistence.filter.SubmissionFilter
import ac.uk.ebi.biostd.persistence.service.SubmissionRepository
import ac.uk.ebi.biostd.submission.submitter.SubmissionSubmitter
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.Submission
import ebi.ac.uk.persistence.PersistenceContext
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import ebi.ac.uk.security.integration.model.api.SecurityUser

class SubmissionService(
    private val submissionRepository: SubmissionRepository,
    private val persistenceContext: PersistenceContext,
    private val serializationService: SerializationService,
    private val userPrivilegesService: IUserPrivilegesService,
    private val submitter: SubmissionSubmitter
) {
    fun getSubmissionAsJson(accNo: String): String {
        val submission = submissionRepository.getByAccNo(accNo)
        return serializationService.serializeSubmission(submission, JsonPretty)
    }

    fun getSubmissionAsXml(accNo: String): String {
        val submission = submissionRepository.getByAccNo(accNo)
        return serializationService.serializeSubmission(submission, XmlFormat)
    }

    fun getSubmissionAsTsv(accNo: String): String {
        val submission = submissionRepository.getByAccNo(accNo)
        return serializationService.serializeSubmission(submission, Tsv)
    }

    fun getSubmissions(user: SecurityUser, filter: SubmissionFilter) =
        submissionRepository.getSubmissionsByUser(user.id, filter)

    fun deleteSubmission(accNo: String, user: SecurityUser) {
        val submission = persistenceContext.getSubmission(accNo)!!

        require(userPrivilegesService.canDelete(user.email, submission.user, submission.accessTags))
        submissionRepository.expireSubmission(accNo)
    }

    fun submit(submission: Submission, user: SecurityUser, files: FilesSource): Submission =
        submitter.submit(ExtendedSubmission(submission, user.asUser()), files, persistenceContext)
}
