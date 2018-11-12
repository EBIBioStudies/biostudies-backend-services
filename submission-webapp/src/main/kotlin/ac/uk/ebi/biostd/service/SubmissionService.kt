package ac.uk.ebi.biostd.service

import ac.uk.ebi.biostd.json.JsonSerializer
import ac.uk.ebi.biostd.persistence.service.SubmissionRepository
import ac.uk.ebi.biostd.submission.model.PersistenceContext
import ac.uk.ebi.biostd.tsv.TsvSerializer
import ac.uk.ebi.biostd.xml.XmlSerializer
import arrow.core.Option
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.User

class SubmissionService(
    private val submissionRepository: SubmissionRepository,
    private val jsonSerializer: JsonSerializer,
    private val tsvSerializer: TsvSerializer,
    private val xmlSerializer: XmlSerializer
) {

    fun getSubmissionAsJson(accNo: String): String {
        val submission = submissionRepository.findByAccNo(accNo)
        return jsonSerializer.serialize(submission)
    }

    fun getSubmissionAsXml(accNo: String): String {
        val submission = submissionRepository.findByAccNo(accNo)
        return xmlSerializer.serialize(submission)
    }

    fun getSubmissionAsTsv(accNo: String): String {
        val submission = submissionRepository.findByAccNo(accNo)
        return tsvSerializer.serialize(submission)
    }

    fun submitSubmission(submission: Submission, user: User): Submission {
        throw NotImplementedError()
    }

    companion object : PersistenceContext {

        override fun getSequenceNextValue(name: String): Long {
            return 5000
        }

        override fun getParentAccessTags(submissionDb: Submission): List<String> {
            return emptyList()
        }

        override fun getParentAccPattern(submissionDb: Submission): Option<String> {
            return Option.empty()
        }
    }
}
