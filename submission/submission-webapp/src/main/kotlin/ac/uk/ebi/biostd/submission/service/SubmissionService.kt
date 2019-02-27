package ac.uk.ebi.biostd.submission.service

import ac.uk.ebi.biostd.json.JsonSerializer
import ac.uk.ebi.biostd.persistence.service.SubmissionRepository
import ac.uk.ebi.biostd.submission.SubmissionSubmitter
import ac.uk.ebi.biostd.submission.model.ResourceFile
import ac.uk.ebi.biostd.tsv.serialization.TsvToStringSerializer
import ac.uk.ebi.biostd.xml.XmlSerializer
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.User
import ebi.ac.uk.persistence.PersistenceContext

class SubmissionService(
    private val submissionRepository: SubmissionRepository,
    private val persistenceContext: PersistenceContext,
    private val jsonSerializer: JsonSerializer,
    private val tsvSerializer: TsvToStringSerializer,
    private val xmlSerializer: XmlSerializer,
    private val submitter: SubmissionSubmitter
) {

    fun getSubmissionAsJson(accNo: String): String {
        val submission = submissionRepository.getByAccNo(accNo)
        return jsonSerializer.serialize(submission)
    }

    fun getSubmissionAsXml(accNo: String): String {
        val submission = submissionRepository.getByAccNo(accNo)
        return xmlSerializer.serialize(submission)
    }

    fun getSubmissionAsTsv(accNo: String): String {
        val submission = submissionRepository.getByAccNo(accNo)
        return tsvSerializer.serialize(submission)
    }

    fun deleteSubmission(accNo: String, user: User) {
        require(persistenceContext.canDelete(accNo, user))
        submissionRepository.expireSubmission(accNo)
    }

    fun submit(
        submission: Submission,
        user: User,
        files: List<ResourceFile> = emptyList()
    ) = submitter.submit(ExtendedSubmission(submission, user), files, persistenceContext)
}
