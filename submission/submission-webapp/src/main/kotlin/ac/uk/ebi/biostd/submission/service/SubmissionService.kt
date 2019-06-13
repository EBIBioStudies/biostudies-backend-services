package ac.uk.ebi.biostd.submission.service

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.persistence.service.SubmissionRepository
import ac.uk.ebi.biostd.submission.SubmissionSubmitter
import ac.uk.ebi.biostd.submission.model.UserSource
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.User
import ebi.ac.uk.persistence.PersistenceContext
import ebi.ac.uk.security.integration.model.api.SecurityUser

class SubmissionService(
    private val submissionRepository: SubmissionRepository,
    private val persistenceContext: PersistenceContext,
    private val serializationService: SerializationService,
    private val submitter: SubmissionSubmitter
) {

    fun getSubmissionAsJson(accNo: String): String {
        val submission = submissionRepository.getByAccNo(accNo)
        return serializationService.serializeSubmission(submission, SubFormat.JSON_PRETTY)
    }

    fun getSubmissionAsXml(accNo: String): String {
        val submission = submissionRepository.getByAccNo(accNo)
        return serializationService.serializeSubmission(submission, SubFormat.XML)
    }

    fun getSubmissionAsTsv(accNo: String): String {
        val submission = submissionRepository.getByAccNo(accNo)
        return serializationService.serializeSubmission(submission, SubFormat.TSV)
    }

    fun deleteSubmission(accNo: String, user: SecurityUser) {
        require(persistenceContext.canDelete(accNo, asUser(user)))
        submissionRepository.expireSubmission(accNo)
    }

    fun submit(
        submission: Submission,
        user: SecurityUser,
        files: UserSource
    ) = submitter.submit(ExtendedSubmission(submission, asUser(user)), files, persistenceContext)

    private fun asUser(securityUser: SecurityUser): User =
        User(securityUser.id, securityUser.email, securityUser.secret)
}
