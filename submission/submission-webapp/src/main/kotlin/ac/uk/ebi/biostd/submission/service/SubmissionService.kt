package ac.uk.ebi.biostd.submission.service

import ac.uk.ebi.biostd.integration.ISerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.persistence.service.SubmissionRepository
import ac.uk.ebi.biostd.submission.SubmissionSubmitter
import ac.uk.ebi.biostd.submission.model.UserSource
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.User
import ebi.ac.uk.persistence.PersistenceContext

class SubmissionService(
    private val submissionRepository: SubmissionRepository,
    private val persistenceContext: PersistenceContext,
    private val serializationService: ISerializationService,
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

    fun deleteSubmission(accNo: String, user: User) {
        require(persistenceContext.canDelete(accNo, user))
        submissionRepository.expireSubmission(accNo)
    }

    fun submit(
        submission: Submission,
        user: User,
        files: UserSource
    ) = submitter.submit(ExtendedSubmission(submission, user), files, persistenceContext)
}
